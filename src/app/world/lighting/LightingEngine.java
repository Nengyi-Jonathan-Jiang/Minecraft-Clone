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
import java.util.function.Function;

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
            Vector3i adjacentPos = blockPosition.add(faceDirection.direction, new Vector3i());

            if(!world.isBlockLoaded(adjacentPos.x, adjacentPos.y, adjacentPos.z)) return new Vector4f(.5f);

            int baseBlockLight = world.getBlockLightAt(adjacentPos.x, adjacentPos.y, adjacentPos.z);
            float lightMultiplier = faceDirection.lightMultiplier;

            float faceLight = baseBlockLight * lightMultiplier / 16f;

            Function<Vector3i, Float> getLightAt = (x) -> {
                return 0f;
            };

            float lv00 =

            return new Vector4f(faceLight, 1, 1, 0);
        }

        private float distanceToSegment(Vector3f a, Vector3f b, Vector3f v){
            return (b.sub(a, new Vector3f()).cross(v.sub(a, new Vector3f()))).length() / 2;
        }

        public Vector2f getInterpolatorForPoint(Vector3f point, BlockModel.FaceDirection faceDirection) {
            // TODO: make interpolation logic

            // Get rid of the component in the direction of the face normal
            var mask = new Vector3f(faceDirection.direction).absolute().sub(1, 1, 1).absolute();
            var flattenedPoint = mask.mul(point);

            float x = distanceToSegment(
                new Vector3f(faceDirection.v00),
                new Vector3f(faceDirection.v01),
                flattenedPoint
            );
            float y = distanceToSegment(
                    new Vector3f(faceDirection.v00),
                    new Vector3f(faceDirection.v10),
                    flattenedPoint
            );

            return new Vector2f(x, y);
        }

        private int calculateWeightedAOValue(int current, int side1, int side2, int corner){
            return ((int)(current * .5 + side1 * .2 + side2 * .2 + corner * .1));
        }
    }

    public AOData getAOData(Vector3i pos){
        return new AOData(pos);
    }

    public void recalculateLighting(Collection<Chunk> chunksToUpdate) {
        System.out.println("Recalculating lighting for " + chunksToUpdate.size() + " chunks");
        // Propagate light to any neighboring chunks if needed

        PriorityQueue<LightingUpdate> lightingUpdates = new PriorityQueue<>();

        for(Chunk chunk : chunksToUpdate) {
            chunk.getLightingData().clear();

            for (int x = 0; x < Chunk.SIZE; x++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    //Set the light level of all blocks exposed to skylight to max
                    for (int y = Chunk.HEIGHT - 1; y >= 0; y--) {
                        int trueX = x + chunk.getChunkPosition().x * Chunk.SIZE;
                        //noinspection UnnecessaryLocalVariable
                        int trueY = y;
                        int trueZ = z + chunk.getChunkPosition().y * Chunk.SIZE;

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
        int lightingStep;
        int maxLightingUpdates = 64 * chunksToUpdate.size() * Chunk.SIZE * Chunk.SIZE * Chunk.HEIGHT;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            var update = lightingUpdates.poll();
            Vector3i pos = update.pos;
            int x = pos.x, y = pos.y, z = pos.z, lightLevel = update.lightLevel;

            if(isOutOfRange(new Vector3i(x, y, z), chunksToUpdate)) continue;

            //If this lighting update is outdated, skip it
            if (lightLevel != world.getBlockLightAt(x, y, z)) continue;

            //For each adjacent block
            for (Vector3i offset : Arrays.stream(BlockModel.FaceDirection.OUTER_FACES).map(i -> i.direction).toList()) {
                //Get the position
                Vector3i newPos = offset.add(pos, new Vector3i());

                // TODO: confine light to updating chunks
                if (this.isOutOfRange(newPos, chunksToUpdate)) continue;
                int newBlockID = world.getBlockIDAt(newPos.x, newPos.y, newPos.z);

                if(newBlockID != 0 && !BlockRegistry.getBlock(newBlockID).hasTag("transparent")) continue;
                // Hey cool idea: transparent blocks do not attenuate light. Like fiber optics

                //This is the light value that will be propagated to the block
                int newLightLevel = lightLevel - 1;

                //Don't update the light level if it is lower than the current level
                if (newLightLevel > this.getBlockLightAt(newPos, chunksToUpdate)) {
                    //Set light level of the block
                    this.setBlockLightAt(newPos, newLightLevel, chunksToUpdate);
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

    private boolean isOutOfRange(Vector3i pos, Collection<Chunk> chunksToUpdate) {
        // TODO: make more efficient
        if(!world.isBlockLoaded(pos.x, pos.y, pos.z)) return true;
        Chunk containingChunk = world.getChunkForPosition(pos.x, pos.z);
        if(!chunksToUpdate.contains(containingChunk)) return true;
        return !containingChunk.isInRange(0, pos.y, 0);
    }

    public int getBlockLightAt(Vector3i pos, Collection<Chunk> chunks) {
        if(isOutOfRange(pos, chunks)) return 15;

        return world.getBlockLightAt(pos.x, pos.y, pos.z);
    }

    private void setBlockLightAt(Vector3i pos, int level, Collection<Chunk> chunks) {
        if(isOutOfRange(pos, chunks)) return;

        world.setBlockLightAt(pos.x, pos.y, pos.z, level);
    }
}
