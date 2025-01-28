package app.world;

import app.player.Player;
import app.world.chunk.Chunk;
import app.world.chunk.ChunkLoader;
import app.world.chunk.ChunkLoader.ChunkLoaderTask;
import app.world.chunk.ChunkNeighborhood;
import app.world.lighting.LightingEngine;
import app.world.util.ChunkOffset;
import app.world.util.WorldPosition;
import app.world.worldgen.WorldGenerator;
import util.Resource;

import java.util.*;

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

    public Chunk requestChunk(ChunkOffset chunkPosition, boolean forceImmediate) {
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

    public Chunk getChunkIfLoaded(ChunkOffset chunkPosition) {
        ChunkLoaderTask task = loadedChunks.get(chunkPosition);
        if (task == null) return null;
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
        return requestChunk(pos.getChunkOffset(), true)
            .getBlockIDAt(pos.getPositionInChunk());
    }

    public void setBlockIDAt(WorldPosition pos, int id) {
        requestChunk(pos.getChunkOffset(), true).setBlockAt(pos.getPositionInChunk(), id);
    }

    public int getBlockLightAt(WorldPosition pos) {
        return requestChunk(pos.getChunkOffset(), true).getLightingData().getBlockLightAt(pos.getPositionInChunk());
    }

    public void setBlockLightAt(WorldPosition pos, int level) {
        setBlockLightAt(pos, level, true);
    }

    public void setBlockLightAt(WorldPosition pos, int level, boolean markMeshesAsDirty) {
        Chunk chunk = requestChunk(pos.getChunkOffset(), true);
        chunk.getLightingData().setBlockLightAt(pos.getPositionInChunk(), level);

        if (markMeshesAsDirty) {
            getLoadedNeighbors(chunk).forEach(Chunk::invalidateMesh);
        }
    }

    public ChunkNeighborhood getLoadedNeighbors(Chunk chunk) {
        ChunkOffset chunkOffset = chunk.getChunkOffset();
        return getLoadedNeighbors(chunkOffset);
    }

    public boolean isChunkLoaded(ChunkOffset chunkOffset) {
        ChunkLoaderTask task = loadedChunks.get(chunkOffset);
        return task != null && task.get() != null;
    }

    public ChunkNeighborhood getLoadedNeighbors(ChunkOffset chunkPos) {
        return new ChunkNeighborhood(
            getChunkIfLoaded(chunkPos.add(-16, 0, -16, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(-16, 0, 0, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(-16, 0, 16, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(0, 0, -16, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(0, 0, 0, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(0, 0, 16, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(16, 0, -16, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(16, 0, 0, new ChunkOffset())),
            getChunkIfLoaded(chunkPos.add(16, 0, 16, new ChunkOffset()))
        );
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