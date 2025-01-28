package app.world.worldgen;

import app.block.Block;
import app.block.BlockRegistry;
import app.noise.FastNoiseLiteSimplexNoise;
import app.noise.INoise;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import app.world.util.WorldPosition;

// TODO: run on GPU
public class WorldGenerator {
    public INoise noiseGenerator = new FastNoiseLiteSimplexNoise();
    
    public Chunk generateChunk(ChunkOffset chunkOffset, World world) {

        Chunk result = new Chunk(chunkOffset, world);

        addTerrainLayer(result);
        convertTopLayersToGrass(result);
        addBedrockLayer(result);

        result.getLightingData().invalidateAll();



        return result;
    }

    private void convertTopLayersToGrass(Chunk result) {
        for(PositionInChunk pos : PositionInChunk.allPositionsInChunk()) {
            Block thisBlock = result.getBlockAt(pos);

            if (thisBlock == null) continue;

            if (thisBlock.name.equals("dirt")) {
                if (pos.y() == World.CHUNK_HEIGHT - 1 || result.getBlockIDAt(pos.add(0, 1, 0, new PositionInChunk())) == 0) {
                    result.setBlockAt(pos, BlockRegistry.getBlockID("grass"), false);
                }
            }
        }
    }

    private void addTerrainLayer(Chunk chunk) {


        for (PositionInChunk pos : PositionInChunk.allPositionsInChunk()) {
            WorldPosition truePos = pos.getWorldPosition(chunk.getChunkOffset());

            float perturb_size = 60f;

            float perturbX = noiseGenerator.getNoise(truePos.x() / 50f, truePos.y() / 50f, truePos.z() / 50f);
            float perturbY = noiseGenerator.getNoise(truePos.x() / 50f + 9.2f, truePos.y() / 50f - 2.432f, truePos.z() / 50f + 3.52f);
            float perturbZ = noiseGenerator.getNoise(truePos.x() / 50f + 2.34f, truePos.y() / 50f + 4.37f, truePos.z() / 50f + 9.84f);
//            float perturbX = 0;
//            float perturbY = 0;
//            float perturbZ = 0;

            float xx = truePos.x() + perturbX * perturb_size;
            float yy = truePos.y() + perturbY * perturb_size;
            float zz = truePos.z() + perturbZ * perturb_size;

            float noise = noiseGenerator.getNoise(xx / 50f, zz / 50f);
            float height = noise * 30f + 64f;

            if (yy < height - 3) {
                chunk.setBlockAt(pos, BlockRegistry.getBlockID("stone"), false);
            }

            if (height - 3 <= yy && yy < height) {
                chunk.setBlockAt(pos, BlockRegistry.getBlockID("dirt"), false);
            }
        }
    }

    private void addBedrockLayer(Chunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                chunk.setBlockAt(new PositionInChunk(x, 0, z), BlockRegistry.getBlockID("bedrock"), false);
            }
        }
    }
}