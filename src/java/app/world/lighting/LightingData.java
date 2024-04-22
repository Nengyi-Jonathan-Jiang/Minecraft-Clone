package app.world.lighting;

import app.SmallVec3i;
import app.world.World;
import org.joml.Vector2i;
import org.joml.Vector3i;
import util.MathUtil;
import util.Vector3iWrapper;

import java.util.*;

public class LightingData {
    private final int[][][] blockLight = new int[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
    private final LimitedIntSet dirtyBlocks = new LimitedIntSet(World.BLOCKS_PER_CHUNK);
    private final Vector2i chunkPosition;

    public LightingData(Vector2i chunkPosition) {

        this.chunkPosition = chunkPosition;
    }

    public void clear() {
        for (int[][] i : blockLight) for (int[] j : i) Arrays.fill(j, 0);
    }

    public int getBlockLightAt(Vector3i positionInChunk) {
        if (positionInChunk.y >= World.CHUNK_HEIGHT) return 15;

        int x = MathUtil.clamp(positionInChunk.x, 0, World.CHUNK_SIZE - 1);
        int y = MathUtil.clamp(positionInChunk.y, 0, World.CHUNK_HEIGHT - 1);
        int z = MathUtil.clamp(positionInChunk.z, 0, World.CHUNK_SIZE - 1);

        return blockLight[x][y][z];
    }

    public void setBlockLightAt(Vector3i positionInChunk, int level) {
        blockLight[positionInChunk.x][positionInChunk.y][positionInChunk.z] = level;
    }

    public void markBlockDirty(Vector3i positionInChunk) {
        blockLight[positionInChunk.x][positionInChunk.y][positionInChunk.z] = -1;
        this.dirtyBlocks.add(new SmallVec3i(positionInChunk).getBits());
    }

    public void markAllDirty() {
        for(int[][] i : blockLight) for (int[] j : i) Arrays.fill(j, -1);
        this.dirtyBlocks.setAll();
    }

    public Iterable<Vector3i> getDirtyBlocks() {
        return () -> new Iterator<>() {
            final Iterator<Integer> it = dirtyBlocks.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Vector3i next() {
                return new SmallVec3i(it.next()).toVec3i().add(new Vector3i(chunkPosition.x, 0, chunkPosition.y));
            }
        };
    }

    public boolean isBlockDirty() {

    }
}