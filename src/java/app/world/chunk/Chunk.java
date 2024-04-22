package app.world.chunk;

import app.block.Block;
import app.block.BlockRegistry;
import app.util.ChunkOffset;
import app.util.PositionInChunk;
import app.world.World;
import app.world.lighting.LightingData;
import j3d.graph.Mesh;

public class Chunk {
    public final World world;
    final int[][][] data;
    private final LightingData lightingData;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private final ChunkOffset chunkOffset;
    private boolean shouldRebuildMesh = true;
    private Mesh mesh = new Mesh(new int[0]);

    public Chunk(ChunkOffset chunkOffset, World world) {
        this.chunkOffset = chunkOffset;
        this.world = world;
        data = new int[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
        lightingData = new LightingData(chunkOffset);
    }

    public static boolean isYInRange(int y) {
        return y >= 0 && y < World.CHUNK_SIZE;
    }

    public static boolean isInRange(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < World.CHUNK_SIZE && isYInRange(y);
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

    public void setBlockAt(PositionInChunk pos, int id) {
        setBlockAt(pos.x(), pos.y(), pos.z(), id);
    }

    public int getBlockIDAt(int x, int y, int z) {
        return data[x][y][z];
    }

    public int getBlockIDAt(PositionInChunk pos) {
        return getBlockIDAt(pos.x(), pos.y(), pos.z());
    }

    public Block getBlockAt(int x, int y, int z) {
        return BlockRegistry.getBlock(getBlockIDAt(x, y, z));
    }

    public Block getBlockAt(PositionInChunk pos) {
        return getBlockAt(pos.x(), pos.y(), pos.z());
    }

    public ChunkOffset getChunkOffset() {
        return chunkOffset;
    }

    public LightingData getLightingData() {
        return lightingData;
    }
}
