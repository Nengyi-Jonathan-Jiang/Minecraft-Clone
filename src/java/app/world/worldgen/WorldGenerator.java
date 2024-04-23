package app.world.worldgen;

import app.block.Block;
import app.block.BlockRegistry;
import app.noise.FastNoiseLiteSimplexNoise;
import app.noise.INoise;
import app.util.ChunkOffset;
import app.world.World;
import app.world.chunk.Chunk;

public class WorldGenerator {
    public INoise noiseGenerator = new FastNoiseLiteSimplexNoise();
    
    public Chunk generateChunk(ChunkOffset chunkOffset, World world) {
        int chunkOffsetX = chunkOffset.x();
        int chunkOffsetZ = chunkOffset.z();

        Chunk result = new Chunk(chunkOffset, world);

        addTerrainLayer(chunkOffsetX, chunkOffsetZ, result);
        convertTopLayersToGrass(result);
        addBedrockLayer(result);

        return result;
    }

    private void convertTopLayersToGrass(Chunk result) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
                    Block thisBlock = result.getBlockAt(x, y, z);

                    if (thisBlock == null) continue;

                    if (thisBlock.getName().equals("dirt")) {
                        if (y == World.CHUNK_HEIGHT - 1 || result.getBlockIDAt(x, y + 1, z) == 0) {
                            result.setBlockAt(x, y, z, BlockRegistry.getBlockID("grass"));
                        }
                    }
                }
            }
        }
    }

    private void addTerrainLayer(int chunkOffsetX, int chunkOffsetZ, Chunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                int trueX = chunkOffsetX + x;
                int trueZ = chunkOffsetZ + z;

                for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
                    float perturb_size = 30f;

                    float perturbX = noiseGenerator.getNoise(trueX / 50f, y / 50f, trueZ / 50f);
                    float perturbY = noiseGenerator.getNoise(trueX / 50f + 9.2f, y / 50f - 2.432f, trueZ / 50f + 3.52f);
                    float perturbZ = noiseGenerator.getNoise(trueX / 50f + 2.34f, y / 50f + 4.37f, trueZ / 50f + 9.84f);

                    float xx = trueX + perturbX * perturb_size;
                    float yy = y + perturbY * perturb_size;
                    float zz = trueZ + perturbZ * perturb_size;

                    float noise =
                            .25f * noiseGenerator.getNoise(xx / 20f, zz / 20f)
                                    + .5f * noiseGenerator.getNoise(xx / 40f, zz / 40f)
                                    + 1.f * noiseGenerator.getNoise(xx / 80f, zz / 80f)
                                    + 2.f * noiseGenerator.getNoise(xx / 160f, zz / 160f);
                    float height = noise * 5f + 64f;

                    if (yy < height - 3) {
                        chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("stone"));
                    }

                    if (height - 3 <= yy && yy < height) {
                        chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("dirt"));
                    }
                }
            }
        }
    }

    private void addBedrockLayer(Chunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                chunk.setBlockAt(x, 0, z, BlockRegistry.getBlockID("bedrock"));
            }
        }
    }
}
