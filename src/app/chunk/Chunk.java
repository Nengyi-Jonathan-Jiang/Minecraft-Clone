package app.chunk;

import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import j3d.graph.Mesh;

import java.util.ArrayList;

public class Chunk {
    public static int SIZE, HEIGHT;

    private final int[][][] data;
    private boolean shouldRebuildMesh = true;
    private final ChunkMeshBuilder chunkMeshBuilder;
    private Mesh mesh = new Mesh(new float[0], new float[0], new int[0]);

    public Chunk() {
        data = new int[SIZE][HEIGHT][SIZE];
    }

    private void rebuildMesh() {
        // TODO: build mesh


        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if(shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }
}
