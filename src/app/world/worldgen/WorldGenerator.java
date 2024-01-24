package app.world.worldgen;

import app.block.BlockRegistry;
import app.world.chunk.Chunk;
import org.joml.SimplexNoise;
import org.joml.Vector2i;

public class WorldGenerator {
    public Chunk generateChunk(Vector2i chunkPosition) {
        int chunkOffsetX = chunkPosition.x * Chunk.SIZE;
        int chunkOffsetZ = chunkPosition.y * Chunk.SIZE;

        Chunk result = new Chunk();

        addBedrockLayer(chunkOffsetX, chunkOffsetZ, result);
        addTerrainLayer(chunkOffsetX, chunkOffsetZ, result);

        return result;
    }

    private void addTerrainLayer(int chunkOffsetX, int chunkOffsetZ, Chunk chunk) {
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                int trueX = chunkOffsetX + x;
                int trueZ = chunkOffsetZ + z;

                float noise =
                          .25f * SimplexNoise.noise(trueX / 5f, trueZ / 5f)
                        +  .5f * SimplexNoise.noise(trueX / 10f, trueZ / 10f)
                        +  1.f * SimplexNoise.noise(trueX / 20f, trueZ / 20f);
                int height = (int)(noise * 5.5 + 10);

                for(int y = 1; y < height - 3; y++) {
                    chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("stone"));
                }

                for(int y = 1; y < height - 1; y++) {
                    chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("dirt"));
                }

                chunk.setBlockAt(x, height, z, BlockRegistry.getBlockID("grass"));
            }
        }
    }

    private void addBedrockLayer(int chunkOffsetX, int chunkOffsetZ, Chunk chunk) {
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                chunk.setBlockAt(x + chunkOffsetX, 0, z + chunkOffsetZ, BlockRegistry.getBlockID("bedrock"));
            }
        }
    }
}
