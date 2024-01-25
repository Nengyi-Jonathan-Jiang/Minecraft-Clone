package app.world.chunk;

import app.block.Block;
import app.block.BlockRegistry;
import app.world.World;
import app.world.lighting.LightingData;
import j3d.graph.Mesh;
import org.joml.Vector2i;

public class Chunk {
    public static int SIZE = 16, HEIGHT = 256;

    final int[][][] data;
    private final LightingData lightingData;
    private boolean shouldRebuildMesh = true;
    private boolean shouldRecalculateLighting = true;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private Mesh mesh = new Mesh(new float[0], new float[0], new int[0]);
    private final Vector2i chunkPosition;
    private final World world;

    public Chunk(Vector2i chunkPosition, World world) {
        this.chunkPosition = chunkPosition;
        this.world = world;
        data = new int[SIZE][HEIGHT][SIZE];
        lightingData = new LightingData();
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(world, this);
        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if(shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }

    public boolean shouldRecalculateLighting() {
        return shouldRecalculateLighting;
    }

    public void setBlockAt(int x, int y, int z, int id) {
        data[x][y][z] = id;
        shouldRebuildMesh = true;
        shouldRecalculateLighting = true;
    }

    public int getBlockIDAt(int x, int y, int z) {
        return data[x][y][z];
    }

    public Block getBlockAt(int x, int y, int z) {
        return BlockRegistry.getBlock(getBlockIDAt(x, y, z));
    }

    public Vector2i getChunkPosition() {
        return chunkPosition;
    }

    public LightingData getLightingData() {
        return lightingData;
    }

    public boolean isInRange(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < SIZE && y < HEIGHT && z < SIZE;
    }
}
