package app.chunk;

import app.atlas.TextureAtlas;
import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.block.model.PartialMesh;
import app.block.model.PartialMeshVertex;
import j3d.graph.Mesh;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class ChunkMeshBuilder {
    private final List<Float> positions = new ArrayList<>(), uvs = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    public Mesh build(int[][][] data) {
        positions.clear();
        uvs.clear();
        indices.clear();

        int currIndex = 0;

        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int y = 0; y < Chunk.HEIGHT; y++) {
                for(int z = 0; z < Chunk.SIZE; z++) {
                    int blockID = data[x][y][z];
                    Vector3i pos = new Vector3i(x, y, z);

                    Block block = BlockRegistry.getBlock(blockID);
                    BlockModel blockModel = block.getModel();
                    Vector2i texOffset = block.getTexOffset();

                    if(y > Chunk.HEIGHT - 1) currIndex += addFace(currIndex, texOffset, pos, blockModel.top());
                    if(z < Chunk.SIZE - 1)   currIndex += addFace(currIndex, texOffset, pos, blockModel.front());
                    if(x > Chunk.SIZE - 1)   currIndex += addFace(currIndex, texOffset, pos, blockModel.right());
                    if(y > 0)                currIndex += addFace(currIndex, texOffset, pos, blockModel.bottom());
                    if(z > 0)                currIndex += addFace(currIndex, texOffset, pos, blockModel.back());
                    if(x > 0)                currIndex += addFace(currIndex, texOffset, pos, blockModel.left());
                }
            }
        }

        return toMesh();
    }

    private Mesh toMesh() {
        float[] positions = new float[this.positions.size()], uvs = new float[this.uvs.size()];
        int[] indices = new int[this.indices.size()];

        for(int i = 0; i < positions.length; i++) positions[i] = this.positions.get(i);
        for(int i = 0; i < uvs.length;       i++) uvs[i]       = this.uvs.get(i);
        for(int i = 0; i < indices.length;   i++) indices[i]   = this.indices.get(i);

        return new Mesh(positions, uvs, indices);
    }

    private int addFace(int currIndex, Vector2i texOffset, Vector3i chunkPosition, PartialMesh face) {
        PartialMeshVertex[] vertices = face.getVertices();

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

        return vertices.length;
    }
}
