package app.world.lighting;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import app.world.util.WorldPosition;

import java.util.*;
import java.util.stream.Collectors;

// Cursed data structure
public class LightingUpdateQueue {
    private final Map<ChunkOffset, LightingUpdateChunk> chunks = new TreeMap<>();

    Queue<LightingUpdate> queue = new LinkedList<>();

    private static class LightingUpdateChunk {
        private final boolean[] needsUpdate = new boolean[World.BLOCKS_PER_CHUNK];
        private final Chunk chunk;

        private LightingUpdateChunk(Chunk chunk) {
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
    ){

    }

    public LightingUpdateQueue(Collection<Chunk> chunksToUpdate) {
        for(var chunk : chunksToUpdate) {
            chunks.put(chunk.getChunkOffset(), new LightingUpdateChunk(chunk));
        }
    }

    public boolean isOutOfRange(WorldPosition pos) {
        return chunks.containsKey(pos.getChunkOffset());
    }

    public void offer(WorldPosition position) {
        LightingUpdateChunk chunk = chunks.get(position.getChunkOffset());
        if(chunk == null) return;
        // We already have this update in queue
        if(chunk.isDirty(position.getPositionInChunk())) return;

        chunk.setDirty(position.getPositionInChunk());
        queue.offer(new LightingUpdate(chunk, position));
    }

    public LightingUpdate poll() {
        LightingUpdate res = queue.poll();
        res.containingChunk().setNotDirty(res.worldPosition().getPositionInChunk());
        return res;
    }
}
