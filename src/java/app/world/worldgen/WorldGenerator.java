package app.world.worldgen;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;

public interface WorldGenerator {
    Chunk generateChunk(ChunkOffset chunkOffset, World world);
}
