package app.world.worldgen;

import app.block.Block;
import app.block.BlockRegistry;
import app.world.World;
import app.world.chunk.Chunk;
import org.joml.SimplexNoise;
import org.joml.Vector2i;

public class WorldGenerator {
    public Chunk generateChunk(Vector2i chunkPosition, World world) {
        int chunkOffsetX = chunkPosition.x;
        int chunkOffsetZ = chunkPosition.y;

        Chunk result = new Chunk(chunkPosition, world);

        addTerrainLayer(chunkOffsetX, chunkOffsetZ, result);
        convertTopLayersToGrass(result);
        addBedrockLayer(result);

        return result;
    }

    private void convertTopLayersToGrass(Chunk result) {
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                for(int y = 0; y < Chunk.HEIGHT; y++) {
                    Block thisBlock = result.getBlockAt(x, y, z);

                    if(thisBlock == null) continue;

                    if(thisBlock.getName().equals("dirt")) {
                        if(y == Chunk.HEIGHT - 1 || result.getBlockIDAt(x, y + 1, z) == 0) {
                            result.setBlockAt(x, y, z, BlockRegistry.getBlockID("grass"));
                        }
                    }
                }
            }
        }
    }

    private void addTerrainLayer(int chunkOffsetX, int chunkOffsetZ, Chunk chunk) {
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                int trueX = chunkOffsetX + x;
                int trueZ = chunkOffsetZ + z;

                for(int y = 0; y < Chunk.HEIGHT; y++) {
                    float perturbX = SimplexNoise.noise(trueX / 50f, y / 50f, trueZ / 50f);
                    float perturbY = SimplexNoise.noise(trueX / 50f + 9.2f, y / 50f - 2.432f, trueZ / 50f + 3.52f);
                    float perturbZ = SimplexNoise.noise(trueX / 50f + 2.34f, y / 50f + 4.37f, trueZ / 50f + 9.84f);

                    float xx = trueX + perturbX * 30f;
                    float yy = y + perturbY * 30f;
                    float zz = trueZ + perturbZ * 30f;

                    float noise =
                            .25f * SimplexNoise.noise(xx / 20f, zz / 20f)
                          +  .5f * SimplexNoise.noise(xx / 40f, zz / 40f)
                          +  1.f * SimplexNoise.noise(xx / 80f, zz / 80f)
                          +  2.f * SimplexNoise.noise(xx / 160f, zz / 160f);
                    float height = noise * 5f + 64f;

                    if(yy < height - 3) {
                        chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("stone"));
                    }

                    if(height - 3 <= yy && yy < height) {
                        chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("dirt"));
                    }
                }
            }
        }
    }

    private void addBedrockLayer(Chunk chunk) {
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                chunk.setBlockAt(x, 0, z, BlockRegistry.getBlockID("bedrock"));
            }
        }
    }
}
