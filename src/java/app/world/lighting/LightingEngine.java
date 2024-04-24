package app.world.lighting;

import app.block.model.BlockModel.FaceDirection;
import app.world.util.PositionInChunk;
import app.world.util.Vec3i;
import app.world.util.WorldPosition;
import app.world.World;
import app.world.chunk.Chunk;
import util.UniqueQueue;

import java.util.*;

public class LightingEngine {
    public static final boolean useAO = true;

    private final World world;
    private final Set<Chunk> dirtyChunks = new HashSet<>();

    public LightingEngine(World world) {
        this.world = world;
    }

    private static boolean isOutOfRange(WorldPosition pos, LightingEngineUpdateParameters parameters) {
        if (parameters.isOutOfRange(pos)) return true;
        return !Chunk.isYInRange(pos.y());
    }

    public void markPositionAsDirty(WorldPosition pos) {
        Chunk chunk = world.getOrLoadChunk(pos);
        chunk.getLightingData().markBlockDirty(pos.getPositionInChunk());
        dirtyChunks.addAll(world.getLoadedNeighbors(chunk));
    }

    public boolean needsUpdate() {
        return !dirtyChunks.isEmpty();
    }

    public void updateLighting() {
        if(!needsUpdate()) return;

        LightingEngineUpdateParameters parameters = new LightingEngineUpdateParameters(dirtyChunks);

        System.out.println("Updating " + dirtyChunks.size() + " chunks");

        UniqueQueue<WorldPosition> lightingUpdates = new UniqueQueue<>();
        for(var c : dirtyChunks) {
            c.getLightingData().getDirtyBlocks().forEach(lightingUpdates::offer);
        }

        //While update queue is not empty
        long lightingStep;
        long maxLightingUpdates = (long) dirtyChunks.size() * World.BLOCKS_PER_CHUNK * 6;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            updateLightingStep(lightingUpdates, parameters);
        }

        if (lightingStep == maxLightingUpdates) {
            System.out.println("Warning: too many lighting updates.");
        }
        System.out.println("Updated lighting in " + lightingStep + " steps");

        dirtyChunks.forEach(Chunk::markMeshAsDirty);

        for(var i : dirtyChunks) i.getLightingData().clearDirtyBlocks();

        dirtyChunks.clear();
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
            world.setBlockLightAt(pos.x(), pos.y(), pos.z(), newBlockLight, false);
        }
    }

    public AOData getAOData(WorldPosition pos) {
        return new AOData(world, pos);
    }

    public void invalidateLighting(LightingEngineUpdateParameters parameters) {
        System.out.println("Invalidating lighting for " + parameters.chunksToUpdate.size() + " chunks");
        for (Chunk chunk : parameters.chunksToUpdate) {
            Collection<Chunk> loadedNeighbors = world.getLoadedNeighbors(chunk);

            dirtyChunks.addAll(loadedNeighbors);

            chunk.getLightingData().markAllDirty();

//            for (PositionInChunk x : chunk.getLightingData().getDirtyBlocks()) {
//                System.out.println(x + " in " + chunk);
//            }
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

}
