package app.world.lighting;

import app.util.ChunkOffset;
import app.util.WorldPosition;
import app.world.chunk.Chunk;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LightingEngineUpdateParameters {
    public final Set<Chunk> chunksToUpdate;
    public final Set<ChunkOffset> updateChunkPositions;

    public LightingEngineUpdateParameters(Collection<Chunk> chunksToUpdate) {
        this.chunksToUpdate = new HashSet<>(chunksToUpdate);
        this.updateChunkPositions = chunksToUpdate.stream().map(Chunk::getChunkOffset).collect(Collectors.toSet());
    }

    public boolean isOutOfRange(WorldPosition pos) {
        return !updateChunkPositions.contains(ChunkOffset.fromAbsolutePosition(pos));
    }
}
