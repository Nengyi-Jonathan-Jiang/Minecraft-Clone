package app.world.lighting;

import app.block.Block;
import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.world.chunk.Chunk;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.PriorityQueue;

// TODO: make lighting work cross-chunk. We would use a new class that works world-wise, not chunk-wise
// TODO: extract lighting algorithm from class.
public class LightingData {
    private final int[][][] blockLight;
    private final Vector3i[][][] lightingSource;

    public LightingData() {
        blockLight = new int[Chunk.SIZE][Chunk.HEIGHT][Chunk.SIZE];
        lightingSource = new Vector3i[Chunk.SIZE][Chunk.HEIGHT][Chunk.SIZE];
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

    public void update(int[][][] data) {
        for(int[][] i : blockLight)
            for(int[] j : i)
                Arrays.fill(j, 0);

        PriorityQueue<LightingUpdate> lightingUpdates = new PriorityQueue<>();

        //For each column
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                //Set the light level of all blocks exposed to skylight to max
                for (int y = Chunk.HEIGHT - 2; y >= 0; y--) {
                    int blockID = data[x][y][z];

                    boolean isTransparent = blockID == 0 || BlockRegistry.getBlock(blockID).hasTag("transparent");

                    // TODO: add emissive blocks

                    if(!isTransparent) break;

                    //Set the light level of the block
                    blockLight[x][y][z] = 255;
                    lightingSource[x][y][z] = new Vector3i(x, y + 1, z);
                    //Add the update to the priority queue
                    lightingUpdates.offer(new LightingUpdate(new Vector3i(x, y, z), 255));
                }
            }
        }

        //While update queue is not empty
        int lightingStep;
        for (lightingStep = 0; lightingStep < 64 * Chunk.SIZE * Chunk.SIZE * Chunk.HEIGHT && !lightingUpdates.isEmpty(); lightingStep++) {
            var update = lightingUpdates.poll();
            Vector3i pos = update.pos;
            int x = pos.x, y = pos.y, z = pos.z, lightLevel = update.lightLevel;

            Block b = BlockRegistry.getBlock(data[x][y][z]);

            //If this lighting update is outdated, skip it
            if (lightLevel != blockLight[x][y][z]) continue;

            //For each adjacent block
            for (Vector3i offset : new Vector3i[]{
                new Vector3i( 1, 0, 0),
                new Vector3i( 0, 1, 0),
                new Vector3i( 0, 0, 1),
                new Vector3i(-1, 0, 0),
                new Vector3i( 0,-1, 0),
                new Vector3i( 0, 0,-1),
            }) {
                //Get the position
                var newPos = offset.add(pos);
                //Don't propagate lighting across chunk borders (yet - will implement later)
                if (!this.isInRange(newPos)) continue;
                int newBlockID = data[newPos.x][newPos.y][newPos.z];
                if(newBlockID == 0) continue;
                if(!BlockRegistry.getBlock(newBlockID).hasTag("transparent")) continue;
                // Hey cool idea: transparent blocks do not attenuate light. Like fiber optics

                //This is the light value that will be propagated to the block
                int newLightLevel = lightLevel - 1;
                //Don't update the light level if it is lower than the current level
                if (newLightLevel > this.getBlockLightAt(newPos)) {
                    //Set light level of the block
                    this.setBlockLightAt(newPos, newLightLevel);
                    //Set where the light comes from
                    this.lightingSource[newPos.x][newPos.y][newPos.z] = pos;
                    //Add this update to the priority queue
                    lightingUpdates.offer(new LightingUpdate(newPos, newLightLevel));
                }
            }
        }

        System.out.println("Updated lighting in " + lightingStep + " steps");
    }

    private boolean isInRange(Vector3i pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.z >= 0
                && pos.x < Chunk.SIZE && pos.y < Chunk.HEIGHT && pos.z < Chunk.SIZE;
    }

    public int[][][] getBlockLight() {
        return blockLight;
    }

    // TODO: when migrate lighting to world, should store world in chunk and access light through borders
    // This shouldn't even be a thing, really
    public int getBlockLightAt(Vector3i pos) {
        int x = Math.clamp(pos.x, 0, Chunk.SIZE - 1);
        int y = Math.clamp(pos.y, 0, Chunk.HEIGHT - 1);
        int z = Math.clamp(pos.z, 0, Chunk.SIZE - 1);

        return blockLight[x][y][z];
    }

    private void setBlockLightAt(Vector3i pos, int level) {
        blockLight[pos.x][pos.y][pos.z] = level;
    }
}
