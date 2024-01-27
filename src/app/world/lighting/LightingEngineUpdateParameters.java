package app.world.lighting;

import app.world.chunk.Chunk;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LightingEngineUpdateParameters {
    public final Set<Chunk> chunksToUpdate;
    private final Set<Vector2i> updateChunkPositions;

    public LightingEngineUpdateParameters(Collection<Chunk> chunksToUpdate) {
        this.chunksToUpdate = new HashSet<>(chunksToUpdate);
        this.updateChunkPositions = chunksToUpdate.stream().map(Chunk::getChunkPosition).collect(Collectors.toSet());
    }

    public boolean isOutOfRange(Vector3i pos) {
        return !updateChunkPositions.contains(new Vector2i(pos.x >> 4, pos.y >> 4));
    }
}
