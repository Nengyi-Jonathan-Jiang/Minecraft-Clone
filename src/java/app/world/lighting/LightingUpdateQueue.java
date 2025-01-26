package app.world.lighting;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import app.world.util.WorldPosition;

import java.util.*;

public class LightingUpdateQueue {
    private final Map<ChunkOffset, LightingUpdateChunk> chunks = new TreeMap<>();

    Queue<LightingUpdate> queue = new LinkedList<>();

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public static class LightingUpdateChunk {
        private final boolean[] needsUpdate = new boolean[World.BLOCKS_PER_CHUNK];
        private final Chunk chunk;

        public LightingUpdateChunk(Chunk chunk) {
            this.chunk = chunk;
        }

        public Chunk getChunk() {
            return chunk;
        }

        public void setDirty(PositionInChunk positionInChunk) {
            needsUpdate[positionInChunk.getBits()] = true;
        }

        public void setNotDirty(PositionInChunk positionInChunk) {
            needsUpdate[positionInChunk.getBits()] = false;
        }

        public boolean isDirty(PositionInChunk positionInChunk) {
            return needsUpdate[positionInChunk.getBits()];
        }
    }

    public record LightingUpdate(
        LightingUpdateChunk containingChunk,
        WorldPosition worldPosition
    ) {

    }

    public LightingUpdateQueue(Collection<LightingUpdateChunk> chunksToUpdate) {
        for (var chunk : chunksToUpdate) {
            chunks.put(chunk.getChunk().getChunkOffset(), chunk);

            for (WorldPosition worldPosition : chunk.getChunk().getLightingData().getDirtyBlocks()) {
                chunk.setDirty(worldPosition.getPositionInChunk());
                queue.offer(new LightingUpdate(chunk, worldPosition));
            }
        }
    }

    public int size() {
        return queue.size();
    }

    public Chunk getChunkAtPos(WorldPosition pos) {
        if (!Chunk.isYInRange(pos.y())) return null;
        LightingUpdateChunk lightingChunk = chunks.get(pos.getChunkOffset());
        if (lightingChunk == null) return null;
        return lightingChunk.getChunk();
    }

    public void offer(WorldPosition position) {
        LightingUpdateChunk chunk = chunks.get(position.getChunkOffset());
        if (chunk == null) return;
        // We already have this update in queue
        if (chunk.isDirty(position.getPositionInChunk())) return;

        chunk.setDirty(position.getPositionInChunk());
        queue.offer(new LightingUpdate(chunk, position));
    }

    public LightingUpdate poll() {
        LightingUpdate res = queue.poll();
        res.containingChunk().setNotDirty(
            res.worldPosition().getPositionInChunk()
        );
        return res;
    }
}
