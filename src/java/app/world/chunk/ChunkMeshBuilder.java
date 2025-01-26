package app.world.chunk;

import app.atlas.TextureAtlas;
import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.block.model.BlockModel.FaceDirection;
import app.block.model.PartialMesh;
import app.block.model.PartialMeshVertex;
import app.world.World;
import app.world.lighting.AOData;
import app.world.util.IVec3i;
import app.world.util.PositionInChunk;
import app.world.util.Vec3i;
import app.world.util.WorldPosition;
import j3d.graph.Mesh;
import j3d.graph.Mesh.MeshAttributeData;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import util.ArrayBuilder.FloatArrayBuilder;
import util.ArrayBuilder.IntArrayBuilder;

class ChunkMeshBuilder {
    private final FloatArrayBuilder
        positions = new FloatArrayBuilder(),
        uvs = new FloatArrayBuilder(),
        cornerAO = new FloatArrayBuilder(),
        aoInterpolation = new FloatArrayBuilder();

    private final IntArrayBuilder indices = new IntArrayBuilder();

    private boolean shouldShowFace(Vec3i pos, int id, int[] data) {
        if (!Chunk.isInRange(pos)) return true;

        int otherID = data[IVec3i.copy(pos, new PositionInChunk()).getBits()];

        if (otherID == 0) return true;
        if (otherID == id) return false;

        Block block = BlockRegistry.getBlock(otherID);

        return block.hasTag("transparent");
    }

    public Mesh build(World world, Chunk chunk) {
        positions.clear();
        uvs.clear();
        cornerAO.clear();
        aoInterpolation.clear();
        indices.clear();

        int currIndex = 0;

        for (PositionInChunk pos : Chunk.allPositionsInChunk()) {
            int blockID = chunk.getBlockIDAt(pos);

            // Don't generate a mesh for air blocks
            if (blockID == 0) continue;

            Block block = BlockRegistry.getBlock(blockID);

            BlockModel blockModel = block.model;
            Vector2i texOffset = block.texOffset;

            boolean hasVisibleFace = false;

            AOData aoData = world.getLightingEngine().getAOData(
                pos.add(new WorldPosition(
                    chunk.getChunkOffset().x(),
                    0,
                    chunk.getChunkOffset().z()
                ), new WorldPosition())
            );

            for (FaceDirection direction : FaceDirection.OUTER_FACES) {
                Vec3i newPos = pos.add(direction.direction, new Vec3i());
                if (shouldShowFace(newPos, blockID, chunk.data)) {
                    hasVisibleFace = true;
                    currIndex += addFace(currIndex, texOffset, pos, blockModel, direction, aoData);
                }
            }

            if (hasVisibleFace) {
                currIndex += addFace(currIndex, texOffset, pos, blockModel, FaceDirection.INNER, aoData);
            }
        }

        return toMesh();
    }

    private Mesh toMesh() {
        int[] indexData = this.indices.get();
        MeshAttributeData positionData = toMeshAttributeData(3, positions);
        MeshAttributeData uvData = toMeshAttributeData(2, uvs);
        MeshAttributeData cornerAOData = toMeshAttributeData(4, cornerAO);
        MeshAttributeData aoInterpolationData = toMeshAttributeData(2, aoInterpolation);

        positions.clear();
        uvs.clear();
        cornerAO.clear();
        aoInterpolation.clear();
        indices.clear();

        return new Mesh(
            indexData,
            positionData,
            uvData,
            cornerAOData,
            aoInterpolationData
        );
    }

    private MeshAttributeData toMeshAttributeData(int elementSize, FloatArrayBuilder data) {
        float[] arr = data.get();
        return MeshAttributeData.create(elementSize, arr);
    }

    private int addFace(int currIndex, Vector2i texOffset, PositionInChunk chunkPosition, BlockModel model, FaceDirection direction, AOData aoData) {
        PartialMesh face = model.getFace(direction);

        PartialMeshVertex[] vertices = face.vertices();

        Vector4f aoCorners = aoData.getAOForPoint(direction);

        for (PartialMeshVertex vertex : vertices) {
            positions.addAll(
                vertex.x() + chunkPosition.x(),
                vertex.y() + chunkPosition.y(),
                vertex.z() + chunkPosition.z()
            );
            uvs.addAll(
                (texOffset.x + vertex.tx()) * TextureAtlas.scaleFactorX(),
                (texOffset.y + vertex.ty()) * TextureAtlas.scaleFactorY()
            );

            Vector2f aoInterpolator = aoData.getInterpolatorForPoint(vertex.pos(), direction);

            cornerAO.addAll(aoCorners.x, aoCorners.y, aoCorners.z, aoCorners.w);
            aoInterpolation.addAll(aoInterpolator.x, aoInterpolator.y);
        }

        for (int index : face.indices()) {
            indices.addAll(index + currIndex);
        }

        return vertices.length;
    }
}
