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
    private final Map<ChunkOffset, Chunk> loadedChunks = new HashMap<>();
    private final WorldGenerator worldGenerator;
    private final LightingEngine lightingEngine;

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
        lightingEngine = new LightingEngine(this);
    }

    public static ChunkOffset getChunkOffset(int x, int z) {
        return new ChunkOffset(x, z);
    }

    public Chunk getChunkForPosition(int x, int z) {
        ChunkOffset chunkPos = getChunkOffset(x, z);
        return getChunk(chunkPos.x(), chunkPos.z());
    }

    public void loadChunkAtPosition(int x, int z) {
        getOrLoadChunk(getChunkOffset(x, z));
    }

    private Chunk getChunk(int chunkX, int chunkZ) {
        ChunkOffset chunkPosition = new ChunkOffset(chunkX, chunkZ);
        return getOrLoadChunk(chunkPosition);
    }

    private Chunk getOrLoadChunk(ChunkOffset chunkPosition) {
        return loadedChunks.computeIfAbsent(chunkPosition, (__) ->
            worldGenerator.generateChunk(chunkPosition, this)
        );
    }

    public void invalidateLightingForAllVisibleChunks() {
        lightingEngine.invalidateLighting(new LightingEngineUpdateParameters(getVisibleChunks()));
    }

    public void recalculateLighting() {
        lightingEngine.updateLighting();
    }

    public Collection<Chunk> getVisibleChunks() {
        return loadedChunks.values().stream().toList();
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return loadedChunks.containsKey(getChunkOffset(x, z)) && y >= 0 && y < CHUNK_HEIGHT;
    }

    public int getBlockIDAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getBlockIDAt(x & 15, y, z & 15);
    }

    public void setBlockIDAt(int x, int y, int z, int id) {
        getChunkForPosition(x, z).setBlockAt(x & 15, y, z & 15, id);
        lightingEngine.markPositionAsDirty(new WorldPosition(x, y, z));
    }

    public int getBlockLightAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getLightingData().getBlockLightAt(new PositionInChunk(x, y, z));
    }

    public void setBlockLightAt(int x, int y, int z, int level) {
        Chunk chunk = getChunkForPosition(x, z);
        chunk.getLightingData().setBlockLightAt(new PositionInChunk(x, y, z), level);
        getLoadedNeighbors(chunk).forEach(Chunk::markMeshAsDirty);
    }

    public Collection<Chunk> getLoadedNeighbors(Chunk chunk) {
        ChunkOffset chunkPos = chunk.getChunkOffset();
        return getLoadedNeighbors(chunkPos);
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