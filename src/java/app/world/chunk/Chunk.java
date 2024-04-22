package app.world.chunk;

import app.block.Block;
import app.block.BlockRegistry;
import app.world.World;
import app.world.lighting.LightingData;
import j3d.graph.Mesh;
import org.joml.Vector2i;

public class Chunk {
    public final World world;
    final int[][][] data;
    private final LightingData lightingData;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private final Vector2i chunkPosition;
    private boolean shouldRebuildMesh = true;
    private Mesh mesh = new Mesh(new int[0]);

    public Chunk(Vector2i chunkPosition, World world) {
        this.chunkPosition = chunkPosition;
        this.world = world;
        data = new int[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
        lightingData = new LightingData(chunkPosition);
    }

    public static boolean isInRange(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < World.CHUNK_SIZE && y < World.CHUNK_HEIGHT && z < World.CHUNK_SIZE;
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(world, this);
        shouldRebuildMesh = false;
    }

    public Mesh getMesh() {
        if (shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }

    public void markMeshAsDirty() {
        shouldRebuildMesh = true;
    }

    public void setBlockAt(int x, int y, int z, int id) {
        data[x][y][z] = id;
        shouldRebuildMesh = true;
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
}
