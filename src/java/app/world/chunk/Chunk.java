package app.world.chunk;

import app.block.Block;
import app.block.BlockRegistry;
import app.world.World;
import app.world.lighting.LightingData;
import app.world.util.ChunkOffset;
import app.world.util.IVec3i;
import app.world.util.PositionInChunk;
import j3d.graph.Mesh;

import java.util.Iterator;

public class Chunk {
    public final World world;
    final int[] data = new int[World.BLOCKS_PER_CHUNK];
    private final LightingData lightingData;
    private final ChunkMeshBuilder chunkMeshBuilder = new ChunkMeshBuilder();
    private final ChunkOffset chunkOffset;
    private boolean shouldRebuildMesh = true;
    private boolean shouldUpdateLighting = true;
    private Mesh mesh = null;

    public static Iterable<PositionInChunk> allPositionsInChunk() {
        return () -> new Iterator<>() {
            PositionInChunk current = new PositionInChunk(0);

            @Override
            public boolean hasNext() {
                return current.getBits() != 0x10000;
            }

            @Override
            public PositionInChunk next() {
                PositionInChunk res = new PositionInChunk(current.getBits());
                current = new PositionInChunk(current.getBits() + 1);
                return res;
            }
        };
    }

    public Chunk(ChunkOffset chunkOffset, World world) {
        this.chunkOffset = chunkOffset;
        this.world = world;
        lightingData = new LightingData(this);
    }

    public static boolean isYInRange(int y) {
        return y >= 0 && y < World.CHUNK_HEIGHT;
    }

    private static boolean isXInRange(int x) {
        return x >= 0 && x < World.CHUNK_SIZE;
    }

    private static boolean isZInRange(int z) {
        return z >= 0 && z < World.CHUNK_SIZE;
    }

    public static boolean isInRange(int x, int y, int z) {
        return isXInRange(x) && isYInRange(y) && isZInRange(z);
    }

    public static boolean isInRange(IVec3i pos) {
        return isInRange(pos.x(), pos.y(), pos.z());
    }

    private void rebuildMesh() {
        mesh = chunkMeshBuilder.build(world, this);
        shouldRebuildMesh = false;
    }

    public boolean shouldUpdateLighting() {
        return shouldUpdateLighting;
    }

    public Mesh getMesh() {
        if (shouldRebuildMesh) {
            rebuildMesh();
        }
        return mesh;
    }

    public void invalidateMesh() {
        shouldRebuildMesh = true;
    }

    public void setBlockAt(PositionInChunk pos, int id) {
        setBlockAt(pos, id, true);
    }

    public void setBlockAt(PositionInChunk pos, int id, boolean updateLighting) {
        data[pos.getBits()] = id;
        shouldRebuildMesh = true;

        if (updateLighting) {
            shouldUpdateLighting = true;
            world.getLightingEngine().markChunkAsDirty(this);
            lightingData.markBlockDirty(pos);
        }
    }

    public int getBlockIDAt(PositionInChunk pos) {
        return data[pos.getBits()];
    }

    public Block getBlockAt(PositionInChunk pos) {
        return BlockRegistry.getBlock(getBlockIDAt(pos));
    }

    public ChunkOffset getChunkOffset() {
        return chunkOffset;
    }

    public LightingData getLightingData() {
        return lightingData;
    }

    @Override
    public String toString() {
        return "Chunk{" +
            "chunkOffset=" + chunkOffset +
            '}';
    }
}
