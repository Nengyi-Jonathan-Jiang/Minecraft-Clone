package app.world;

import app.world.chunk.Chunk;
import app.world.lighting.LightingEngine;
import app.world.lighting.LightingEngineUpdateParameters;
import app.world.worldgen.WorldGenerator;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public static Vector2i getChunkPosition(int x, int z) {
        return new Vector2i(x & ~15, z & ~15);
    }

    private void loadChunk(Vector2i chunkPosition) {
        if(!loadedChunks.containsKey(chunkPosition)) {
            loadedChunks.put(chunkPosition, worldGenerator.generateChunk(chunkPosition, this));
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
        getChunkForPosition(x, z).getLightingData().setBlockLightAt(new Vector3i(x & 15, y, z & 15), level);
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }
}