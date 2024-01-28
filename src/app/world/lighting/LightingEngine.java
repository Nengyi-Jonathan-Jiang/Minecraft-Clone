package app.world.lighting;

import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.world.World;
import app.world.chunk.Chunk;
import org.joml.*;

import java.util.Arrays;
import java.util.PriorityQueue;

import static util.MathUtil.sum;

public class LightingEngine {
    private final World world;
    private static final boolean useAO = true;

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

        private int getLightAtIfLoadedOrDefault(Vector3i p, int defaultValue) {
            return world.isBlockLoaded(p.x, p.y, p.z)
                    ? world.getBlockLightAt(p.x, p.y, p.z)
                    : defaultValue;
        }
        
        // TODO (low priority): refactor for readability.
        public Vector4f getAOForPoint(BlockModel.FaceDirection faceDirection) {
            // The block adjacent to this face.
            Vector3i adjacentPos = blockPosition.add(faceDirection.direction, new Vector3i());

            if(!world.isBlockLoaded(adjacentPos.x, adjacentPos.y, adjacentPos.z))
                return new Vector4f(0);

            int baseBlockLight = world.getBlockLightAt(adjacentPos.x, adjacentPos.y, adjacentPos.z);
            float lightMultiplier = faceDirection.lightMultiplier;

            if(!useAO) return new Vector4f(baseBlockLight * lightMultiplier / 15f);

            // Light levels for corners
            int l_xy = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.t1, faceDirection.t2), baseBlockLight);
            int l_Xy = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.T1, faceDirection.t2), baseBlockLight);
            int l_xY = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.t1, faceDirection.T2), baseBlockLight);
            int l_XY = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.T1, faceDirection.T2), baseBlockLight);

            // Light levels for edges
            int l_x = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.t1), baseBlockLight);
            int l_y = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.t2), baseBlockLight);
            int l_X = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.T1), baseBlockLight);
            int l_Y = getLightAtIfLoadedOrDefault(sum(adjacentPos, faceDirection.T2), baseBlockLight);

            return new Vector4f(
                calculateWeightedAOValue(
                    baseBlockLight, l_x, l_y, l_xy
                ),
                calculateWeightedAOValue(
                    baseBlockLight, l_X, l_y, l_Xy
                ),
                calculateWeightedAOValue(
                    baseBlockLight, l_x, l_Y, l_xY
                ),
                calculateWeightedAOValue(
                    baseBlockLight, l_X, l_Y, l_XY
                )
            ).mul(lightMultiplier / 15f);
        }

        public Vector2f getInterpolatorForPoint(Vector3f point, BlockModel.FaceDirection faceDirection) {
            // Get rid of the component in the direction of the face normal
            var mask = new Vector3f(faceDirection.direction).absolute().sub(1, 1, 1).absolute();
            var flattenedPoint = mask.mul(point);

            float x = flattenedPoint.dot(new Vector3f(faceDirection.T1));
            float y = flattenedPoint.dot(new Vector3f(faceDirection.T2));

            return new Vector2f(x, y);
        }

        private float calculateWeightedAOValue(int current, int side1, int side2, int corner){
            return (current + side1 + side2 + corner) / 4f;
        }
    }

    public AOData getAOData(Vector3i pos){
        return new AOData(pos);
    }

    public void recalculateLighting(LightingEngineUpdateParameters parameters) {
        System.out.println("Recalculating lighting for " + parameters.chunksToUpdate.size() + " chunks");
        // Propagate light to any neighboring chunks if needed

        PriorityQueue<LightingUpdate> lightingUpdates = new PriorityQueue<>();

        for(Chunk chunk : parameters.chunksToUpdate) {
            chunk.getLightingData().clear();

            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    //Set the light level of all blocks exposed to skylight to max
                    for (int y = World.CHUNK_HEIGHT - 1; y >= 0; y--) {
                        int trueX = x + chunk.getChunkPosition().x;
                        //noinspection UnnecessaryLocalVariable
                        int trueY = y;
                        int trueZ = z + chunk.getChunkPosition().y;

                        int blockID = world.getBlockIDAt(trueX, trueY, trueZ);

                        boolean isTransparent = blockID == 0 || BlockRegistry.getBlock(blockID).hasTag("transparent");

                        // TODO: add emissive blocks

                        if(!isTransparent) break;

                        //Set the light level of the block
                        chunk.getLightingData().setBlockLightAt(new Vector3i(x, y, z), 15);
                        chunk.getLightingData().setBlockLightSourceAt(new Vector3i(x, y, z), new Vector3i(x, y + 1, z));
                        //Add the update to the priority queue
                        lightingUpdates.offer(new LightingUpdate(new Vector3i(trueX, trueY, trueZ), 15));
                    }
                }
            }
        }

        //While update queue is not empty
        long lightingStep;
        long maxLightingUpdates = 64L * parameters.chunksToUpdate.size() * World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_HEIGHT;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            var update = lightingUpdates.poll();
            Vector3i pos = update.pos;
            int x = pos.x, y = pos.y, z = pos.z, lightLevel = update.lightLevel;

            // Why is this bad?
            if(isOutOfRange(pos, parameters)) {
//                System.out.println("Skipped lighting update at " + pos + ": position out of range");
                continue;
            }

            //If this lighting update is outdated, skip it
            if (lightLevel != world.getBlockLightAt(x, y, z)) continue;

            //For each adjacent block
            for (Vector3i offset : Arrays.stream(BlockModel.FaceDirection.OUTER_FACES).map(i -> i.direction).toList()) {
                //Get the position
                Vector3i newPos = offset.add(pos, new Vector3i());

                if (this.isOutOfRange(newPos, parameters)) {
                    continue;
                }
                int newBlockID = world.getBlockIDAt(newPos.x, newPos.y, newPos.z);

                if(newBlockID != 0 && !BlockRegistry.getBlock(newBlockID).hasTag("transparent")) {
                    continue;
                }
                // Hey cool idea: transparent blocks do not attenuate light. Like fiber optics

                //This is the light value that will be propagated to the block
                int newLightLevel = lightLevel - 1;

                // Don't propagate darkness below 0
                if(newLightLevel <= 0) continue;

                //Don't update the light level if it is lower than the current level
                if (newLightLevel > this.getBlockLightAt(newPos, parameters)) {
                    //Set light level of the block
                    this.setBlockLightAt(newPos, newLightLevel, parameters);
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
        //lightingSource[newPos.x][newPos.y][newPos.z] = pos;
        // TODO
    }

    private boolean isOutOfRange(Vector3i pos, LightingEngineUpdateParameters parameters) {
        if(parameters.isOutOfRange(pos)) return true;
        return !Chunk.isInRange(0, pos.y, 0);
    }

    public int getBlockLightAt(Vector3i pos, LightingEngineUpdateParameters chunks) {
        if(isOutOfRange(pos, chunks)) return 15;

        return world.getBlockLightAt(pos.x, pos.y, pos.z);
    }

    private void setBlockLightAt(Vector3i pos, int level, LightingEngineUpdateParameters chunks) {
        if(isOutOfRange(pos, chunks)) return;

        world.setBlockLightAt(pos.x, pos.y, pos.z, level);
    }
}
