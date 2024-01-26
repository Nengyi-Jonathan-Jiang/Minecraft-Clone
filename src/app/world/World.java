package app.world;

import app.world.chunk.Chunk;
import app.world.lighting.LightingEngine;
import app.world.worldgen.WorldGenerator;
import org.joml.Vector2i;
import org.joml.Vector3i;
import util.MathUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class World {
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

    private static Vector2i getChunkPosition(int x, int z) {
        return new Vector2i(MathUtil.floorDiv(x, Chunk.SIZE), MathUtil.floorDiv(z, Chunk.SIZE));
    }

    private void loadChunk(Vector2i chunkPosition) {
        if(!loadedChunks.containsKey(chunkPosition)) {
            loadedChunks.put(chunkPosition, worldGenerator.generateChunk(chunkPosition, this));
        }
    }

    public void recalculateLightingForAllVisibleChunks() {
        lightingEngine.recalculateLighting(getVisibleChunks());
    }
    
    public Collection<Chunk> getVisibleChunks() {
        return loadedChunks.values().stream().toList();
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return loadedChunks.containsKey(getChunkPosition(x, z)) && y >= 0 && y < Chunk.HEIGHT;
    }

    public int getBlockIDAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getBlockIDAt(MathUtil.mod(x, Chunk.SIZE), y, MathUtil.mod(z, Chunk.SIZE));
    }

    public int getBlockLightAt(int x, int y, int z) {
        return getChunkForPosition(x, z).getLightingData().getBlockLightAt(new Vector3i(
                MathUtil.mod(x, Chunk.SIZE), y, MathUtil.mod(z, Chunk.SIZE)
        ));
    }

    public void setBlockLightAt(int x, int y, int z, int level) {
        getChunkForPosition(x, z).getLightingData().setBlockLightAt(new Vector3i(
                MathUtil.mod(x, Chunk.SIZE), y, MathUtil.mod(z, Chunk.SIZE)
        ), level);
    }

    public LightingEngine getLightingEngine() {
        return lightingEngine;
    }
}