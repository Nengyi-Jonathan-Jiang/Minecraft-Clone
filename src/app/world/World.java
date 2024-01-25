package app.world;

import app.world.chunk.Chunk;
import app.world.worldgen.WorldGenerator;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class World {
    private final Map<Vector2i, Chunk> loadedChunks = new HashMap<>();
    private final WorldGenerator worldGenerator;

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    public Chunk getChunkForPosition(int x, int z) {
        int chunkX = x / Chunk.SIZE, chunkZ = z / Chunk.SIZE;
        return getChunk(chunkX, chunkZ);
    }
    
    public void loadChunkAtPosition(int x, int z) {
        int chunkX = x / Chunk.SIZE, chunkZ = z / Chunk.SIZE;
        loadChunk(new Vector2i(chunkX, chunkZ));
    }

    private Chunk getChunk(int chunkX, int chunkZ) {
        Vector2i chunkPosition = new Vector2i(chunkX, chunkZ);
        loadChunk(chunkPosition);
        return loadedChunks.get(chunkPosition);
    }

    private void loadChunk(Vector2i chunkPosition) {
        if(!loadedChunks.containsKey(chunkPosition)) {
            loadedChunks.put(chunkPosition, worldGenerator.generateChunk(chunkPosition));
        }
    }
    
    public Collection<Chunk> getVisibleChunks() {
        return loadedChunks.values();
    }
}