package app.chunk;

import j3d.graph.Mesh;

public class Chunk {
    public static int SIZE, HEIGHT;

    private final int[][][] data;
    private boolean shouldRebuildMesh = true;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private Mesh mesh = new Mesh(new float[0], new float[0], new int[0]);

    public Chunk() {
        data = new int[SIZE][HEIGHT][SIZE];
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(data);
        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if(shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }
}
