package app.world;

import app.world.chunk.Chunk;
import app.world.lighting.LightingEngine;
import app.world.util.ChunkOffset;
import app.world.util.WorldPosition;
import app.world.worldgen.WorldGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    public static int CHUNK_HEIGHT = 256;
    public static int CHUNK_SIZE = 16;

    public static int BLOCKS_PER_CHUNK = CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT;

    private final Map<ChunkOffset, Chunk> loadedChunks = new TreeMap<>();
    private final WorldGenerator worldGenerator;
    private final LightingEngine lightingEngine;

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
        lightingEngine = new LightingEngine(this);
    }

    public Chunk getOrLoadChunk(int x, int z) {
        return getOrLoadChunk(new ChunkOffset(x, z));
    }

    public void loadChunk(WorldPosition position) {
        getOrLoadChunkAtPos(position);
    }

    public Chunk getOrLoadChunkAtPos(WorldPosition position) {
        return getOrLoadChunk(position.x(), position.z());
    }

    private Chunk getOrLoadChunk(ChunkOffset chunkPosition) {
        return loadedChunks.computeIfAbsent(chunkPosition, (pos) ->
            worldGenerator.generateChunk(pos, this)
        );
    }

    public void invalidateLightingForAllVisibleChunks() {
        lightingEngine.invalidateLighting(getLoadedChunks());
    }

    public void updateLighting() {
        lightingEngine.updateLighting();
    }

    public Collection<Chunk> getLoadedChunks() {
        return loadedChunks.values().stream().toList();
    }

    public boolean isBlockLoaded(WorldPosition pos) {
        return Chunk.isYInRange(pos.y()) && loadedChunks.containsKey(pos.getChunkOffset());
    }

    public int getBlockIDAt(WorldPosition pos) {
        return getOrLoadChunk(pos.getChunkOffset()).getBlockIDAt(pos.getPositionInChunk());
    }

    public void setBlockIDAt(WorldPosition pos, int id) {
        getOrLoadChunk(pos.getChunkOffset()).setBlockAt(pos.getPositionInChunk(), id);
    }

    public int getBlockLightAt(WorldPosition pos) {
        return getOrLoadChunkAtPos(pos).getLightingData().getBlockLightAt(pos.getPositionInChunk());
    }

    public void setBlockLightAt(WorldPosition pos, int level) {
        setBlockLightAt(pos, level, true);
    }

    public void setBlockLightAt(WorldPosition pos, int level, boolean markMeshesAsDirty) {
        Chunk chunk = getOrLoadChunkAtPos(pos);
        chunk.getLightingData().setBlockLightAt(pos.getPositionInChunk(), level);

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
//        return Stream.of(
//                chunkPos.add(-16, 0, -16, new ChunkOffset()),
//                chunkPos.add(-16, 0,   0, new ChunkOffset()),
//                chunkPos.add(-16, 0,  16, new ChunkOffset()),
//                chunkPos.add(  0, 0, -16, new ChunkOffset()),
//                chunkPos.add(  0, 0,   0, new ChunkOffset()),
//                chunkPos.add(  0, 0,  16, new ChunkOffset()),
//                chunkPos.add( 16, 0, -16, new ChunkOffset()),
//                chunkPos.add( 16, 0,   0, new ChunkOffset()),
//                chunkPos.add( 16, 0,  16, new ChunkOffset())
//        ).map(this::getOrLoadChunk).collect(Collectors.toList());
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }

}