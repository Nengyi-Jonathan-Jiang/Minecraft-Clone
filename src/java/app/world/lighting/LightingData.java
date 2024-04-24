package app.world.lighting;

import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.WorldPosition;

import java.util.Arrays;
import java.util.Iterator;

public class LightingData {
    private final int[] blockLight = new int[World.BLOCKS_PER_CHUNK];
    private final ChunkOffset chunkOffset;

    private final DirtyInts dirtyBlocks = new DirtyInts(World.BLOCKS_PER_CHUNK);

    public LightingData(ChunkOffset chunkOffset) {
        this.chunkOffset = chunkOffset;
    }

    public void clear() {
        Arrays.fill(blockLight, 0);
        dirtyBlocks.clear();
    }

    public int getBlockLightAt(PositionInChunk pos) {
        if (!Chunk.isYInRange(pos.y())) return 15;

        return blockLight[pos.getBits()];
    }

    public void setBlockLightAt(PositionInChunk pos, int level) {
        blockLight[pos.getBits()] = level;
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