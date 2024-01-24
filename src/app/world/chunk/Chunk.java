package app.world.chunk;

import app.world.lighting.LightingData;
import j3d.graph.Mesh;

public class Chunk {
    public static int SIZE = 16, HEIGHT = 256;

    final int[][][] data;
    final LightingData lightingData;
    private boolean shouldRebuildMesh = true;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private Mesh mesh = new Mesh(new float[0], new float[0], new int[0]);

    public Chunk() {
        data = new int[SIZE][HEIGHT][SIZE];
        lightingData = new LightingData();
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(this);
        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if(shouldRebuildMesh) {
            recalculateLighting();
            rebuildMesh();
        }
        return mesh;
    }

    private void recalculateLighting() {
        lightingData.update(this.data);
    }

    public void setBlockAt(int x, int y, int z, int id) {
        data[x][y][z] = id;
        shouldRebuildMesh = true;
    }
}
