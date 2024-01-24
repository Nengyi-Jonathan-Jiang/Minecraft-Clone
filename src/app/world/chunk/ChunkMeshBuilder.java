package app.world.chunk;

import app.atlas.TextureAtlas;
import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.block.model.PartialMesh;
import app.block.model.PartialMeshVertex;
import j3d.graph.Mesh;
import org.joml.Vector2i;
import org.joml.Vector3i;
import util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshBuilder {
    private final List<Float> positions = new ArrayList<>(), uvs = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    private boolean shouldShowFace(int x, int y, int z, int id, int[][][] data) {
        if (x >= Chunk.SIZE || y >= Chunk.HEIGHT || z >= Chunk.SIZE || x < 0 || y < 0 || z < 0) return true;

        int otherID = data[x][y][z];

        if(otherID == 0) return true;
        if(otherID == id) return false;

        Block block = BlockRegistry.getBlock(otherID);

        return block.getTags().contains("transparent");
    }

    public Mesh build(Chunk chunk) {
        positions.clear();
        uvs.clear();
        indices.clear();

        int currIndex = 0;

        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int y = 0; y < Chunk.HEIGHT; y++) {
                for(int z = 0; z < Chunk.SIZE; z++) {
                    int blockID = chunk.data[x][y][z];

                    // Don't generate a mesh for air blocks
                    if(blockID == 0) continue;

                    Vector3i pos = new Vector3i(x, y, z);

                    Block block = BlockRegistry.getBlock(blockID);

                    BlockModel blockModel = block.getModel();
                    Vector2i texOffset = block.getTexOffset();

                    boolean hasVisibleFace = false;

                    if(shouldShowFace(x, y + 1, z, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.top());
                    }
                    if(shouldShowFace(x, y, z + 1, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.front());
                    }
                    if(shouldShowFace(x + 1, y, z, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.right());
                    }
                    if(shouldShowFace(x, y - 1, z, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.bottom());
                    }
                    if(shouldShowFace(x, y, z - 1, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.back());
                    }
                    if(shouldShowFace(x - 1, y, z, blockID, chunk.data)) {
                        hasVisibleFace = true; currIndex += addFace(currIndex, texOffset, pos, blockModel.left());
                    }

                    if(hasVisibleFace) {
                        currIndex += addFace(currIndex, texOffset, pos, blockModel.inner());
                    }
                }
            }
        }

        return toMesh();
    }

    private Mesh toMesh() {
        System.out.println("Created mesh with " + positions.size() / 3 + " vertices, " + indices.size() + " indices");

        return new Mesh(
            ArrayUtil.toFloatArray(positions),
            ArrayUtil.toFloatArray(uvs),
            ArrayUtil.toIntArray(indices)
        );
    }

    private int addFace(int currIndex, Vector2i texOffset, Vector3i chunkPosition, PartialMesh face) {
        PartialMeshVertex[] vertices = face.vertices();

        for(PartialMeshVertex vertex : vertices) {
            positions.addAll(List.of(
                vertex.x() + chunkPosition.x,
                vertex.y() + chunkPosition.y,
                vertex.z() + chunkPosition.z
            ));
            uvs.addAll(List.of(
                (texOffset.x + vertex.tx()) * TextureAtlas.scaleFactorX(),
                (texOffset.y + vertex.ty()) * TextureAtlas.scaleFactorY()
            ));
        }

        for(int index : face.indices()) {
            indices.add(index + currIndex);
        }

        return vertices.length;
    }
}
