package app.world.lighting;

import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.world.World;
import app.world.chunk.Chunk;
import org.joml.*;
import util.MathUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.PriorityQueue;

public class LightingEngine {
    private final World world;

    public LightingEngine(World world) {
        this.world = world;
    }

    private record LightingUpdate(Vector3i pos, int lightLevel) implements Comparable<LightingUpdate> {
        @Override
        public int compareTo(LightingUpdate o2) {
            return lightLevel - o2.lightLevel;
        }
    }

    public class AOData {
        private final Vector3i blockPosition;

        public AOData(Vector3i blockPosition) {
            this.blockPosition = blockPosition;
        }

        public Vector4f getAOForPoint(BlockModel.FaceDirection faceDirection) {
            // TODO: make actual AO
            int baseBlockLight = getBlockLightAt(blockPosition.add(faceDirection.direction, new Vector3i()));
            float lightMultiplier = faceDirection.lightMultiplier;
            float light = baseBlockLight * lightMultiplier / 16f;
            return new Vector4f(light, light, light, light);
        }

        public Vector2f getInterpolatorForPoint(Vector3f point, BlockModel.FaceDirection faceDirection) {
            // TODO: make interpolation logic
            return new Vector2f(0, 0);
        }

        private int calculateWeightedAOValue(int current, int side1, int side2, int corner){
            return ((int)(current * .5 + side1 * .2 + side2 * .2 + corner * .1));
        }
    }

    public AOData getAOData(Vector3i pos){
        return new AOData(pos);
    }

    public void recalculateLighting(Collection<Chunk> chunksToUpdate) {
        // Propagate light to any neighboring chunks if needed

        PriorityQueue<LightingUpdate> lightingUpdates = new PriorityQueue<>();

        for(Chunk c : chunksToUpdate) {
            c.getLightingData().clear();

            for (int x = 0; x < Chunk.SIZE; x++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    //Set the light level of all blocks exposed to skylight to max
                    for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                        int trueX = x + c.getChunkPosition().x * Chunk.SIZE;
                        //noinspection UnnecessaryLocalVariable
                        int trueY = y;
                        int trueZ = z + c.getChunkPosition().y * Chunk.SIZE;

                        int blockID = world.getBlockIDAt(trueX, trueY, trueZ);

                        boolean isTransparent = blockID == 0 || BlockRegistry.getBlock(blockID).hasTag("transparent");

                        // TODO: add emissive blocks

                        if(!isTransparent) break;

                        //Set the light level of the block
                        c.getLightingData().setBlockLightAt(new Vector3i(x, y, z), 15);
                        c.getLightingData().setBlockLightSourceAt(new Vector3i(x, y, z), new Vector3i(x, y + 1, z));
                        //Add the update to the priority queue
                        lightingUpdates.offer(new LightingUpdate(new Vector3i(trueX, trueY, trueZ), 15));
                    }
                }
            }
        }

        //While update queue is not empty
        int lightingStep;
        int maxLightingUpdates = 64 * chunksToUpdate.size() * Chunk.SIZE * Chunk.SIZE * Chunk.HEIGHT;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            var update = lightingUpdates.poll();
            Vector3i pos = update.pos;
            int x = pos.x, y = pos.y, z = pos.z, lightLevel = update.lightLevel;

            //If this lighting update is outdated, skip it
            if (lightLevel != world.getBlockLightAt(x, y, z)) continue;

            //For each adjacent block
            for (Vector3i offset : Arrays.stream(BlockModel.FaceDirection.OUTER_FACES).map(i -> i.direction).toList()) {
                //Get the position
                Vector3i newPos = offset.add(pos, new Vector3i());

                // TODO: confine light to updating chunks
                if (!this.isInRange(newPos, chunksToUpdate)) continue;
                int newBlockID = world.getBlockIDAt(newPos.x, newPos.y, newPos.z);

                if(newBlockID != 0 && !BlockRegistry.getBlock(newBlockID).hasTag("transparent")) continue;
                // Hey cool idea: transparent blocks do not attenuate light. Like fiber optics

                //This is the light value that will be propagated to the block
                int newLightLevel = lightLevel - 1;

                //Don't update the light level if it is lower than the current level
                if (newLightLevel > this.getBlockLightAt(newPos)) {
                    //Set light level of the block
                    this.setBlockLightAt(newPos, newLightLevel);
                    //Set where the light comes from
                    this.setLightingSourceAt(newPos, pos);
                    //Add this update to the priority queue
                    lightingUpdates.offer(new LightingUpdate(newPos, newLightLevel));
                }
            }
        }

        if(lightingStep == maxLightingUpdates) {
            System.out.println("Warning: too many lighting updates.");
        }
        System.out.println("Updated lighting in " + lightingStep + " steps");
    }

    private void setLightingSourceAt(Vector3i newPos, Vector3i pos) {
        lightingSource[newPos.x][newPos.y][newPos.z] = pos;
    }

    private boolean isInRange(Vector3i pos, Collection<Chunk> chunksToUpdate) {

    }

    public int[][][] getBlockLight() {
        return blockLight;
    }

    // TODO: when migrate lighting to world, should store world in chunk and access light through borders
    // This shouldn't even be a thing, really
    public int getBlockLightAt(Vector3i pos) {
        if(pos.y >= Chunk.HEIGHT) return 15;

        int x = MathUtil.clamp(pos.x, 0, Chunk.SIZE - 1);
        int y = MathUtil.clamp(pos.y, 0, Chunk.HEIGHT - 1);
        int z = MathUtil.clamp(pos.z, 0, Chunk.SIZE - 1);

        return blockLight[x][y][z];
    }

    private void setBlockLightAt(Vector3i pos, int level) {
        blockLight[pos.x][pos.y][pos.z] = level;
    }
}
