package app.world;

import app.player.Player;
import app.world.chunk.Chunk;
import app.world.chunk.ChunkLoader;
import app.world.chunk.ChunkLoader.ChunkLoaderTask;
import app.world.lighting.LightingEngine;
import app.world.util.ChunkOffset;
import app.world.util.WorldPosition;
import app.world.worldgen.WorldGenerator;
import util.Resource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World implements Resource {
    public static int CHUNK_HEIGHT = 256;
    public static int CHUNK_SIZE = 16;

    public static int BLOCKS_PER_CHUNK = CHUNK_SIZE * CHUNK_SIZE * CHUNK_HEIGHT;

    private final Map<ChunkOffset, ChunkLoaderTask> loadedChunks = new TreeMap<>();

    private final WorldGenerator worldGenerator;
    private final ChunkLoader loader;
    private final LightingEngine lightingEngine;

    public World(WorldGenerator worldGenerator, Player player) {
        this.worldGenerator = worldGenerator;
        lightingEngine = new LightingEngine(this);

        // TODO: refactor to not need player parameter, somehow.
        loader = new ChunkLoader(this, Comparator.comparingDouble(
            offset -> player.getPosition().distance(offset.toVector3f())
        ));
    }

    public Chunk getOrLoadChunkAtPos(WorldPosition position, boolean forceImmediate) {
        return getOrLoadChunk(position.getChunkOffset(), forceImmediate);
    }

    public Chunk getOrLoadChunk(ChunkOffset chunkPosition, boolean forceImmediate) {
        ChunkLoaderTask task = loadedChunks.get(chunkPosition);
        if (task != null) {
            if (!task.hasResult() && forceImmediate) {
                task.doImmediately();
            }
        } else {
            task = forceImmediate ? loader.requestChunkImmediate(chunkPosition) : loader.requestChunk(chunkPosition);
            loadedChunks.put(chunkPosition, task);
        }

        return task.get();
    }

    public void invalidateLightingForAllVisibleChunks() {
        lightingEngine.invalidateLighting(getLoadedChunks());
    }

    public void updateLighting() {
        lightingEngine.updateLighting();
    }

    public Collection<Chunk> getLoadedChunks() {
        synchronized (loadedChunks) {
            return loadedChunks.values().stream().map(ChunkLoaderTask::get).filter(Objects::nonNull).toList();
        }
    }

    public boolean isBlockLoaded(WorldPosition pos) {
        if (!Chunk.isYInRange(pos.y())) return false;
        ChunkLoaderTask chunk = loadedChunks.get(pos.getChunkOffset());
        if (chunk == null) return false;
        return chunk.get() != null;
    }

    public int getBlockIDAt(WorldPosition pos) {
        return getOrLoadChunk(pos.getChunkOffset(), true)
            .getBlockIDAt(pos.getPositionInChunk());
    }

    public void setBlockIDAt(WorldPosition pos, int id) {
        getOrLoadChunk(pos.getChunkOffset(), true).setBlockAt(pos.getPositionInChunk(), id);
    }

    public int getBlockLightAt(WorldPosition pos) {
        return getOrLoadChunkAtPos(pos, true).getLightingData().getBlockLightAt(pos.getPositionInChunk());
    }

    public void setBlockLightAt(WorldPosition pos, int level) {
        setBlockLightAt(pos, level, true);
    }

    public void setBlockLightAt(WorldPosition pos, int level, boolean markMeshesAsDirty) {
        Chunk chunk = getOrLoadChunkAtPos(pos, true);
        chunk.getLightingData().setBlockLightAt(pos.getPositionInChunk(), level);

        if (markMeshesAsDirty) {
            getLoadedNeighbors(chunk).forEach(Chunk::invalidateMesh);
        }
    }

    public Collection<Chunk> getLoadedNeighbors(Chunk chunk) {
        ChunkOffset chunkOffset = chunk.getChunkOffset();
        return getLoadedNeighbors(chunkOffset);
    }

    public boolean isChunkLoaded(ChunkOffset chunkOffset) {
        ChunkLoaderTask task = loadedChunks.get(chunkOffset);
        return task != null && task.get() != null;
    }

    public Collection<Chunk> getLoadedNeighbors(ChunkOffset chunkPos) {
        return Stream.of(
                chunkPos.add(-16, 0, -16, new ChunkOffset()),
                chunkPos.add(-16, 0, 0, new ChunkOffset()),
                chunkPos.add(-16, 0, 16, new ChunkOffset()),
                chunkPos.add(0, 0, -16, new ChunkOffset()),
                chunkPos.add(0, 0, 0, new ChunkOffset()),
                chunkPos.add(0, 0, 16, new ChunkOffset()),
                chunkPos.add(16, 0, -16, new ChunkOffset()),
                chunkPos.add(16, 0, 0, new ChunkOffset()),
                chunkPos.add(16, 0, 16, new ChunkOffset())
            )
            .map(loadedChunks::get)
            .filter(Objects::nonNull)
            .map(ChunkLoaderTask::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }

    public WorldGenerator getWorldGenerator() {
        return this.worldGenerator;
    }

    @Override
    public void freeResources() {
        this.loader.freeResources();
    }
}