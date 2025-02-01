package app.world.worldgen;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import util.Resource;

public interface WorldGenerator extends Resource {
    Chunk generateChunk(ChunkOffset chunkOffset, World world);

    default void freeResources() {
    }
}
