package app.world;

import app.world.chunk.Chunk;
import app.world.lighting.LightingEngine;
import app.world.lighting.LightingEngineUpdateParameters;
import app.world.worldgen.WorldGenerator;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class World {
    public static int CHUNK_HEIGHT = 256;
    public static int CHUNK_SIZE = 16;
    private final Map<Vector2i, Chunk> loadedChunks = new HashMap<>();
    private final WorldGenerator worldGenerator;
    private final LightingEngine lightingEngine;

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
        lightingEngine = new LightingEngine(this);
    }

    public static Vector2i getChunkPosition(int x, int z) {
        return new Vector2i(x & ~15, z & ~15);
    }

    public Chunk getChunkForPosition(int x, int z) {
        Vector2i chunkPos = getChunkPosition(x, z);
        return getChunk(chunkPos.x, chunkPos.y);
    }

    public void loadChunkAtPosition(int x, int z) {
        loadChunk(getChunkPosition(x, z));
    }

    private Chunk getChunk(int chunkX, int chunkZ) {
        Vector2i chunkPosition = new Vector2i(chunkX, chunkZ);
        loadChunk(chunkPosition);
        return loadedChunks.get(chunkPosition);
    }

    private void loadChunk(Vector2i chunkPosition) {
        if (!loadedChunks.containsKey(chunkPosition)) {
            Chunk chunk = worldGenerator.generateChunk(chunkPosition, this);
            loadedChunks.put(chunkPosition, chunk);
            lightingEngine.recalculateLighting(new LightingEngineUpdateParameters(List.of(chunk)));
        }
    }

    public void recalculateLightingForAllVisibleChunks() {
        lightingEngine.recalculateLighting(new LightingEngineUpdateParameters(getVisibleChunks()));
    }

    public void recalculateLighting() {
        lightingEngine.updateLighting();
    }

    public Collection<Chunk> getVisibleChunks() {
        return loadedChunks.values().stream().toList();
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return loadedChunks.containsKey(getChunkPosition(x, z)) && y >= 0 && y < CHUNK_HEIGHT;
    }

    public int getBlockIDAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getBlockIDAt(x & 15, y, z & 15);
    }

    public void setBlockIDAt(int x, int y, int z, int id) {
        getChunkForPosition(x, z).setBlockAt(x & 15, y, z & 15, id);
        lightingEngine.markPositionAsDirty(new Vector3i(x, y, z));
    }

    public int getBlockLightAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getLightingData().getBlockLightAt(new Vector3i(x & 15, y, z & 15));
    }

    public void setBlockLightAt(int x, int y, int z, int level) {
        Chunk chunk = getChunkForPosition(x, z);
        chunk.getLightingData().setBlockLightAt(new Vector3i(x & 15, y, z & 15), level);
        getLoadedNeighbors(chunk).forEach(Chunk::markMeshAsDirty);
    }

    public Collection<Chunk> getLoadedNeighbors(Chunk chunk) {
        Vector2i pos = chunk.getChunkPosition();
        return Stream.of(
                new Vector2i(pos.x - 16, pos.y - 16),
                new Vector2i(pos.x - 16, pos.y + 0),
                new Vector2i(pos.x - 16, pos.y + 16),
                new Vector2i(pos.x + 0, pos.y - 16),
                new Vector2i(pos.x + 0, pos.y + 0),
                new Vector2i(pos.x + 0, pos.y + 16),
                new Vector2i(pos.x + 16, pos.y - 16),
                new Vector2i(pos.x + 16, pos.y + 0),
                new Vector2i(pos.x + 16, pos.y + 16)
        ).filter(p -> this.isBlockLoaded(p.x, 0, p.y)).map(p -> getChunkForPosition(p.x, p.y)).collect(Collectors.toList());
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }

    public boolean isBlockTransparent(int x, int y, int z) {
        // TODO: add transparent blocks
        return getBlockIDAt(x, y, z) == 0;
    }
}