package app.world.lighting;

import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.WorldPosition;
import util.MathUtil;

import java.util.Arrays;
import java.util.Iterator;

public class LightingData {
    private final int[][][] blockLight;
    private final ChunkOffset chunkOffset;

    private final DirtyInts dirtyBlocks = new DirtyInts(World.BLOCKS_PER_CHUNK);

    public LightingData(ChunkOffset chunkOffset) {
        this.chunkOffset = chunkOffset;
        blockLight = new int[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
    }

    public void clear() {
        for (int[][] i : blockLight) for (int[] j : i) Arrays.fill(j, 0);
        dirtyBlocks.clear();
    }

    public int getBlockLightAt(PositionInChunk pos) {
        if (!Chunk.isYInRange(pos.y())) return 15;

        int x = MathUtil.clamp(pos.x(), 0, World.CHUNK_SIZE - 1);
        int y = MathUtil.clamp(pos.y(), 0, World.CHUNK_HEIGHT - 1);
        int z = MathUtil.clamp(pos.z(), 0, World.CHUNK_SIZE - 1);

        return blockLight[x][y][z];
    }

    public void setBlockLightAt(PositionInChunk pos, int level) {
        blockLight[pos.x()][pos.y()][pos.z()] = level;
    }

    public void markBlockDirty(PositionInChunk pos) {
        dirtyBlocks.markDirty(pos.getBits());
    }

    public void markAllDirty() {
        dirtyBlocks.markAllDirty();
    }

    public void clearDirtyBlocks() {
        dirtyBlocks.clear();
    }

    public Iterable<WorldPosition> getDirtyBlocks(){
        return () -> new Iterator<>() {
            final Iterator<Integer> it = dirtyBlocks.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public WorldPosition next() {
                return new PositionInChunk(it.next()).getAbsolutePosition(chunkOffset);
            }
        };
    }
}