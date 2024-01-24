package app.world.chunk;

import j3d.graph.Mesh;

public class Chunk {
    public static int SIZE = 16, HEIGHT = 256;

    final int[][][] data;
    private boolean shouldRebuildMesh = true;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private Mesh mesh = new Mesh(new float[0], new float[0], new int[0]);

    public Chunk() {
        data = new int[SIZE][HEIGHT][SIZE];
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(this);
        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if(shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }

    public void setBlockAt(int x, int y, int z, int id) {
        data[x][y][z] = id;
    }
}
