package app.world.chunk;

import app.atlas.TextureAtlas;
import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.block.model.BlockModel.FaceDirection;
import app.block.model.PartialMesh;
import app.block.model.PartialMeshVertex;
import app.world.World;
import app.world.lighting.LightingEngine;
import j3d.graph.Mesh;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector4f;
import util.ArrayConcatenator;
import util.ArrayUtil;

public class ChunkMeshBuilder {
    private final ArrayConcatenator<Float> positions = new ArrayConcatenator<>(), uvs = new ArrayConcatenator<>();

    // AO and lighting data should be passed as a vec4 (corner AO values) + 1 vec2 (for interpolation)
    private final ArrayConcatenator<Float> cornerAO = new ArrayConcatenator<>(),
                                           aoInterpolation = new ArrayConcatenator<>();

    private final ArrayConcatenator<Integer> indices = new ArrayConcatenator<>();

    private boolean shouldShowFace(int x, int y, int z, int id, int[][][] data) {
        if (x >= World.CHUNK_SIZE || y >= World.CHUNK_HEIGHT || z >= World.CHUNK_SIZE || x < 0 || y < 0 || z < 0) return true;

        int otherID = data[x][y][z];

        if(otherID == 0) return true;
        if(otherID == id) return false;

        Block block = BlockRegistry.getBlock(otherID);

        return block.getTags().contains("transparent");
    }

    public Mesh build(World world, Chunk chunk) {
        positions.clear();
        uvs.clear();
        cornerAO.clear();
        aoInterpolation.clear();
        indices.clear();

        int currIndex = 0;

        for(int x = 0; x < World.CHUNK_SIZE; x++) {
            for(int y = 0; y < World.CHUNK_HEIGHT; y++) {
                for(int z = 0; z < World.CHUNK_SIZE; z++) {
                    int blockID = chunk.data[x][y][z];

                    // Don't generate a mesh for air blocks
                    if(blockID == 0) continue;

                    Vector3i pos = new Vector3i(x, y, z);

                    Block block = BlockRegistry.getBlock(blockID);

                    BlockModel blockModel = block.getModel();
                    Vector2i texOffset = block.getTexOffset();

                    boolean hasVisibleFace = false;

                    LightingEngine.AOData aoData = world.getLightingEngine().getAOData(
                        pos.add(new Vector3i(
                            chunk.getChunkPosition().x,
                            0,
                            chunk.getChunkPosition().y
                        ), new Vector3i())
                    );

                    for(FaceDirection direction : FaceDirection.OUTER_FACES) {
                        Vector3i newPos = pos.add(direction.direction, new Vector3i());
                        if(shouldShowFace(newPos.x, newPos.y, newPos.z, blockID, chunk.data)) {
                            hasVisibleFace = true;
                            currIndex += addFace(currIndex, texOffset, pos, blockModel, direction, aoData);
                        }
                    }

                    if(hasVisibleFace) {
                        currIndex += addFace(currIndex, texOffset, pos, blockModel, FaceDirection.INNER, aoData);
                    }
                }
            }
        }

        return toMesh();
    }

    private Mesh toMesh() {
//        System.out.println("Created mesh with " + positions.size() / 3 + " vertices, " + indices.size() + " indices");

        float[] positionData = ArrayUtil.unbox(positions.get(Float[]::new));
        float[] uvData = ArrayUtil.unbox(uvs.get(Float[]::new));
        int[] indexData = ArrayUtil.unbox(this.indices.get(Integer[]::new));
        Mesh.FloatAttributeData cornerAOData = new Mesh.FloatAttributeData(4, ArrayUtil.unbox(cornerAO.get(Float[]::new)));
        Mesh.FloatAttributeData aoInterpolationData = new Mesh.FloatAttributeData(2, ArrayUtil.unbox(aoInterpolation.get(Float[]::new)));

        positions.clear();
        uvs.clear();
        cornerAO.clear();
        aoInterpolation.clear();
        indices.clear();

        return new Mesh(positionData, uvData, indexData, cornerAOData, aoInterpolationData);
    }

    private int addFace(int currIndex, Vector2i texOffset, Vector3i chunkPosition, BlockModel model, FaceDirection direction, LightingEngine.AOData aoData) {
        PartialMesh face = model.getFace(direction);

        PartialMeshVertex[] vertices = face.vertices();

        Vector4f aoCorners = aoData.getAOForPoint(direction);

        for(PartialMeshVertex vertex : vertices) {
            positions.addAll(
                vertex.x() + chunkPosition.x,
                vertex.y() + chunkPosition.y,
                vertex.z() + chunkPosition.z
            );
            uvs.addAll(
                (texOffset.x + vertex.tx()) * TextureAtlas.scaleFactorX(),
                (texOffset.y + vertex.ty()) * TextureAtlas.scaleFactorY()
            );

            Vector2f aoInterpolator = aoData.getInterpolatorForPoint(vertex.pos(), direction);

            cornerAO.addAll(aoCorners.x, aoCorners.y, aoCorners.z, aoCorners.w);
            aoInterpolation.addAll(aoInterpolator.x, aoInterpolator.y);
        }

        for(int index : face.indices()) {
            indices.addAll(index + currIndex);
        }

        return vertices.length;
    }
}
