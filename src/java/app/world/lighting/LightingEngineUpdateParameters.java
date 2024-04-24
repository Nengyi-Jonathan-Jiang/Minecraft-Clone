package app.world.lighting;

import app.world.util.ChunkOffset;
import app.world.util.WorldPosition;
import app.world.chunk.Chunk;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LightingEngineUpdateParameters {
    public final Set<Chunk> chunksToUpdate;
    public final Set<ChunkOffset> updateChunkPositions;

    public LightingEngineUpdateParameters(Collection<Chunk> chunksToUpdate) {
        this.chunksToUpdate = new HashSet<>(chunksToUpdate);
        this.updateChunkPositions = chunksToUpdate.stream().map(Chunk::getChunkOffset)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public boolean isOutOfRange(WorldPosition pos) {
        return !updateChunkPositions.contains(ChunkOffset.fromAbsolutePosition(pos));
    }
}
