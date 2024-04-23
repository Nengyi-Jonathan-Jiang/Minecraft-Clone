package app.world;

import app.util.ChunkOffset;
import app.util.PositionInChunk;
import app.util.WorldPosition;
import app.world.chunk.Chunk;
import app.world.lighting.LightingEngine;
import app.world.lighting.LightingEngineUpdateParameters;
import app.world.worldgen.WorldGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    public static int CHUNK_HEIGHT = 256;
    public static int CHUNK_SIZE = 16;

    public static int BLOCKS_PER_CHUNK = CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT;

    private final Map<ChunkOffset, Chunk> loadedChunks = new HashMap<>();
    private final WorldGenerator worldGenerator;
    private final LightingEngine lightingEngine;

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
        lightingEngine = new LightingEngine(this);
    }

    public Chunk getOrLoadChunk(int x, int z) {
        return getOrLoadChunk(new ChunkOffset(x, z));
    }

    public Chunk getOrLoadChunk(WorldPosition position) {
        return getOrLoadChunk(position.x(), position.z());
    }

    private Chunk getOrLoadChunk(ChunkOffset chunkPosition) {
        return loadedChunks.computeIfAbsent(chunkPosition, (pos) ->
            worldGenerator.generateChunk(pos, this)
        );
    }

    public void invalidateLightingForAllVisibleChunks() {
        lightingEngine.invalidateLighting(new LightingEngineUpdateParameters(getLoadedChunks()));
    }

    public void updateLighting() {
        lightingEngine.updateLighting();
    }

    public Collection<Chunk> getLoadedChunks() {
        return loadedChunks.values().stream().toList();
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return Chunk.isYInRange(y) && loadedChunks.containsKey(new ChunkOffset(x, z));
    }

    public boolean isBlockLoaded(WorldPosition pos) {
        return isBlockLoaded(pos.x(), pos.y(), pos.z());
    }

    public int getBlockIDAt(int x, int y, int z) {
        return getOrLoadChunk(x, z).getBlockIDAt(x & 15, y, z & 15);
    }

    public void setBlockIDAt(int x, int y, int z, int id) {
        getOrLoadChunk(x, z).setBlockAt(x & 15, y, z & 15, id);
        lightingEngine.markPositionAsDirty(new WorldPosition(x, y, z));
    }

    public int getBlockLightAt(int x, int y, int z) {
        return getOrLoadChunk(x, z).getLightingData().getBlockLightAt(new PositionInChunk(x, y, z));
    }

    public void setBlockLightAt(int x, int y, int z, int level) {
        setBlockLightAt(x, y, z, level, true);
    }

    public void setBlockLightAt(int x, int y, int z, int level, boolean markMeshesAsDirty) {
        Chunk chunk = getOrLoadChunk(x, z);
        chunk.getLightingData().setBlockLightAt(new PositionInChunk(x, y, z), level);

        if(markMeshesAsDirty) {
            getLoadedNeighbors(chunk).forEach(Chunk::markMeshAsDirty);
        }
    }

    public Collection<Chunk> getLoadedNeighbors(Chunk chunk) {
        ChunkOffset chunkOffset = chunk.getChunkOffset();
        return getLoadedNeighbors(chunkOffset);
    }

    public Collection<Chunk> getLoadedNeighbors(ChunkOffset chunkPos) {
        return Stream.of(
                chunkPos.add(-16, 0, -16, new ChunkOffset()),
                chunkPos.add(-16, 0,   0, new ChunkOffset()),
                chunkPos.add(-16, 0,  16, new ChunkOffset()),
                chunkPos.add(  0, 0, -16, new ChunkOffset()),
                chunkPos.add(  0, 0,   0, new ChunkOffset()),
                chunkPos.add(  0, 0,  16, new ChunkOffset()),
                chunkPos.add( 16, 0, -16, new ChunkOffset()),
                chunkPos.add( 16, 0,   0, new ChunkOffset()),
                chunkPos.add( 16, 0,  16, new ChunkOffset())
        ).filter(loadedChunks::containsKey).map(loadedChunks::get).collect(Collectors.toList());
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }

    public boolean isBlockTransparent(int x, int y, int z) {
        // TODO: add transparent blocks
        return getBlockIDAt(x, y, z) == 0;
    }
}