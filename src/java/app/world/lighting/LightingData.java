package app.world.lighting;

import app.block.Block;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.PositionInChunk;
import app.world.util.WorldPosition;

import java.util.Arrays;
import java.util.Iterator;

public class LightingData {
    private final int[] blockLight = new int[World.BLOCKS_PER_CHUNK];
    private final Chunk chunk;

    private final DirtyInts dirtyBlocks = new DirtyInts(World.BLOCKS_PER_CHUNK);

    public LightingData(Chunk chunk) {
        this.chunk = chunk;
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

    public void invalidateAll() {
        Arrays.fill(blockLight, 0);
        dirtyBlocks.clear();

        // Handle all air blocks exposed to sky light
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                for (int y = World.CHUNK_HEIGHT - 1; y >= 0; y--) {
                    PositionInChunk pos = new PositionInChunk(x, y, z);
                    if (chunk.getBlockIDAt(pos) != 0) {
                        break;
                    }
                    blockLight[pos.getBits()] = 15;
                }
            }
        }

        // Mark all non-solid (completely opaque) blocks as dirty
        for (PositionInChunk pos : PositionInChunk.allPositionsInChunk()) {
            Block block = chunk.getBlockAt(pos);
            // We already handled this block in sky light propagation
            if (blockLight[pos.getBits()] != 0) continue;
            // Set opaque block to block light = 0
            if (block != null && block.opacity == 15) {
                blockLight[pos.getBits()] = 0;
            }
            // Everything else is dirty
            else {
                dirtyBlocks.markDirty(pos.getBits());
            }
        }
//        dirtyBlocks.markAllDirty();
    }

    public void clearDirtyBlocks() {
        dirtyBlocks.clear();
    }

    public Iterable<WorldPosition> getDirtyBlocks() {
        return () -> new Iterator<>() {
            final Iterator<Integer> it = dirtyBlocks.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public WorldPosition next() {
                return new PositionInChunk(it.next()).getWorldPosition(chunk.getChunkOffset());
            }
        };
    }
}