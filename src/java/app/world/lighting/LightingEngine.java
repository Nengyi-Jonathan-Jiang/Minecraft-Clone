package app.world.lighting;

import app.block.model.BlockModel.FaceDirection;
import app.util.IVec3i;
import app.util.Vec3i;
import app.util.WorldPosition;
import app.world.World;
import app.world.chunk.Chunk;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.UniqueQueue;

import java.util.*;

import static util.MathUtil.sum;

public class LightingEngine {
    private static final boolean useAO = true;

    private final World world;
    private final Set<WorldPosition> dirtyPositions = new HashSet<>();

    public LightingEngine(World world) {
        this.world = world;
    }

    private static boolean isOutOfRange(WorldPosition pos, LightingEngineUpdateParameters parameters) {
        if (parameters.isOutOfRange(pos)) return true;
        return !Chunk.isYInRange(pos.y());
    }

    public void markPositionAsDirty(WorldPosition pos) {
        dirtyPositions.add(pos);
    }

    public boolean needsUpdate() {
        return !dirtyPositions.isEmpty();
    }

    public void updateLighting() {
        Set<Chunk> dirtyChunks = computeDirtyChunks();
        LightingEngineUpdateParameters parameters = new LightingEngineUpdateParameters(dirtyChunks);

        System.out.println("Updating " + dirtyPositions.size() + " dirty blocks in " + dirtyChunks.size() + " chunks");

        UniqueQueue<WorldPosition> lightingUpdates = new UniqueQueue<>();
        dirtyPositions.forEach(lightingUpdates::offer);

        //While update queue is not empty
        long lightingStep;
        long maxLightingUpdates = (long) dirtyChunks.size() * World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_HEIGHT * 6;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            updateLightingStep(lightingUpdates, parameters);
        }

        if (lightingStep == maxLightingUpdates) {
            System.out.println("Warning: too many lighting updates.");
        }
        System.out.println("Updated lighting in " + lightingStep + " steps");

        dirtyPositions.clear();
    }

    private void updateLightingStep(UniqueQueue<WorldPosition> lightingUpdates, LightingEngineUpdateParameters parameters) {
        var pos = lightingUpdates.poll();

        int oldBlockLight = world.getBlockLightAt(pos.x(), pos.y(), pos.z());

        int newBlockLight = 0;
        List<WorldPosition> neighbors = new ArrayList<>();

        int opacity = world.isBlockTransparent(pos.x(), pos.y(), pos.z()) ? 0 : 15;

        if(pos.y() == World.CHUNK_HEIGHT - 1) newBlockLight = Math.max(15 - opacity, 0);

        for (var face : FaceDirection.OUTER_FACES) {
            Vec3i offset = face.direction;
            WorldPosition newPos = pos.add(offset, new WorldPosition());

            if (isOutOfRange(newPos, parameters)) {
                continue;
            }

            int neighborLight = world.getBlockLightAt(newPos.x(), newPos.y(), newPos.z());
            int neighborOpacity = world.isBlockTransparent(newPos.x(), newPos.y(), newPos.z()) ? 0 : 15;

            int propagatedLight = face == FaceDirection.TOP && neighborLight == 15 ? 15 - opacity :
                    neighborLight - opacity - neighborOpacity - 1;
            newBlockLight = Math.max(newBlockLight, propagatedLight);

            neighbors.add(newPos);
        }

        // Our light level changed; our neighbors are all suspect
        if (newBlockLight != oldBlockLight) {
            neighbors.forEach(lightingUpdates::offer);
            world.setBlockLightAt(pos.x(), pos.y(), pos.z(), newBlockLight);
        }
    }

    private Set<Chunk> computeDirtyChunks() {
        // limit lighting updates to within 15 blocks of dirty positions
        Set<Chunk> dirtyChunks = new HashSet<>();
        for (WorldPosition pos : dirtyPositions) {
            dirtyChunks.addAll(world.getLoadedNeighbors(world.getChunkForPosition(pos.x(), pos.z())));
        }
        return dirtyChunks;
    }

    public AOData getAOData(WorldPosition pos) {
        return new AOData(pos);
    }

    public void invalidateLighting(LightingEngineUpdateParameters parameters) {
        for (Chunk chunk : parameters.chunksToUpdate) {
            for (int y = World.CHUNK_HEIGHT - 1; y >= 0; y--) {
                for (int x = 0; x < World.CHUNK_SIZE; x++) {
                    for (int z = 0; z < World.CHUNK_SIZE; z++) {
                        int trueX = x + chunk.getChunkOffset().x();
                        //noinspection UnnecessaryLocalVariable
                        int trueY = y;
                        int trueZ = z + chunk.getChunkOffset().z();

                        dirtyPositions.add(new WorldPosition(trueX, trueY, trueZ));
                    }
                }
            }
        }
    }

    public int getBlockLightAt(WorldPosition pos, LightingEngineUpdateParameters chunks) {
        if (isOutOfRange(pos, chunks)) return 15;

        return world.getBlockLightAt(pos.x(), pos.y(), pos.z());
    }

    private void setBlockLightAt(WorldPosition pos, int level, LightingEngineUpdateParameters chunks) {
        if (isOutOfRange(pos, chunks)) return;

        world.setBlockLightAt(pos.x(), pos.y(), pos.z(), level);
    }

    private record LightingUpdate(WorldPosition pos, int lightLevel) implements Comparable<LightingUpdate> {
        @Override
        public int compareTo(LightingUpdate o2) {
            return lightLevel - o2.lightLevel;
        }
    }

    public class AOData {
        private final WorldPosition position;

        public AOData(WorldPosition position) {
            this.position = position;
        }

        private int getLightAtIfLoadedOrDefault(WorldPosition p, int defaultValue) {
            return world.isBlockLoaded(p.x(), p.y(), p.z())
                    ? world.getBlockLightAt(p.x(), p.y(), p.z())
                    : defaultValue;
        }

        // TODO (medium priority): refactor for readability.
        public Vector4f getAOForPoint(FaceDirection faceDirection) {
            // The block adjacent to this face.
            WorldPosition adjacentPos = position.add(faceDirection.direction, new WorldPosition());

            if (!world.isBlockLoaded(adjacentPos.x(), adjacentPos.y(), adjacentPos.z()))
                return new Vector4f(0);

            int baseBlockLight = world.getBlockLightAt(adjacentPos.x(), adjacentPos.y(), adjacentPos.z());
            float lightMultiplier = faceDirection.lightMultiplier;

            if (!useAO) return new Vector4f(baseBlockLight * lightMultiplier / 15f);

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

        public Vector2f getInterpolatorForPoint(Vector3f point, FaceDirection faceDirection) {
            // Get rid of the component in the direction of the face normal
            var mask = new Vector3f(faceDirection.direction).absolute().sub(1, 1, 1).absolute();
            var flattenedPoint = mask.mul(point);

            float x = flattenedPoint.dot(new Vector3f(faceDirection.T1));
            float y = flattenedPoint.dot(new Vector3f(faceDirection.T2));

            return new Vector2f(x, y);
        }

        private float calculateWeightedAOValue(int current, int side1, int side2, int corner) {
            return (current + side1 + side2 + corner) / 4f;
        }
    }
}
