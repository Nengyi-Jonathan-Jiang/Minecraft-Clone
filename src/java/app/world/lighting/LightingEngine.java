package app.world.lighting;

import app.block.model.BlockModel.FaceDirection;
import app.player.Player;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.Vec3i;
import app.world.util.WorldPosition;

import java.util.*;

public class LightingEngine {
    public static final boolean useAO = true;

    private final World world;
    private final Set<Chunk> dirtyChunks = new HashSet<>();

    public LightingEngine(World world) {
        this.world = world;
    }

    public void markChunkAsDirty(Chunk c){
        dirtyChunks.add(c);
    }

    public boolean needsUpdate() {
        return !dirtyChunks.isEmpty();
    }

    public void updateLighting(Player player) {

        if(!needsUpdate()) return;

        System.out.println("Updating " + dirtyChunks.size() + " chunks");

        new ArrayList<>(dirtyChunks).stream().map(world::getLoadedNeighbors).forEach(dirtyChunks::addAll);

        LightingUpdateQueue lightingUpdates = new LightingUpdateQueue(dirtyChunks);

        //While update queue is not empty
        long lightingStep;
        long maxLightingUpdates = (long) dirtyChunks.size() * World.BLOCKS_PER_CHUNK * 6;
        for (lightingStep = 0; lightingStep < maxLightingUpdates && !lightingUpdates.isEmpty(); lightingStep++) {
            updateLightingStep(lightingUpdates);
        }

        if (lightingStep == maxLightingUpdates) {
            System.out.println("Warning: too many lighting updates.");
        }
        System.out.println("Updated lighting in " + lightingStep + " steps");

        dirtyChunks.forEach(Chunk::markMeshAsDirty);

        for(var i : dirtyChunks) i.getLightingData().clearDirtyBlocks();

        dirtyChunks.clear();
    }

    private void updateLightingStep(LightingUpdateQueue lightingUpdates) {
        var update = lightingUpdates.poll();
        WorldPosition pos = update.worldPosition();

        LightingUpdateQueue.LightingUpdateChunk lightingChunk = update.containingChunk();
        Chunk chunk = lightingChunk.getChunk();

        int oldBlockLight = chunk.getLightingData().getBlockLightAt(pos.getPositionInChunk());

        int newBlockLight = 0;
        List<WorldPosition> neighbors = new ArrayList<>();

        int opacity = chunk.getBlockIDAt(pos.getPositionInChunk()) == 0 ? 0 : 15;

        if(pos.y() == World.CHUNK_HEIGHT - 1) newBlockLight = Math.max(15 - opacity, 0);

        for (var face : FaceDirection.OUTER_FACES) {
            Vec3i offset = face.direction;
            WorldPosition newPos = pos.add(offset, new WorldPosition());

            if (lightingUpdates.isOutOfRange(newPos)) {
                continue;
            }

            Chunk neighborChunk = world.getOrLoadChunkAtPos(newPos);
            int neighborLight = neighborChunk.getLightingData().getBlockLightAt(newPos.getPositionInChunk());
            int neighborOpacity = neighborChunk.getBlockAt(newPos.getPositionInChunk()) == null ? 0 : 15;

            int propagatedLight = face == FaceDirection.TOP && neighborLight == 15 ? 15 - opacity :
                    neighborLight - opacity - neighborOpacity - 1;
            newBlockLight = Math.max(newBlockLight, propagatedLight);

            neighbors.add(newPos);
        }

        // Our light level changed; our neighbors are all suspect
        if (newBlockLight != oldBlockLight) {
            neighbors.forEach(lightingUpdates::offer);
            world.setBlockLightAt(pos, newBlockLight, false);
        }
    }

    public AOData getAOData(WorldPosition pos) {
        return new AOData(world, pos);
    }

    public void invalidateLighting(Collection<Chunk> chunksToUpdate) {
        System.out.println("Invalidating lighting for " + chunksToUpdate.size() + " chunks");
        for (Chunk chunk : chunksToUpdate) {
            Collection<Chunk> loadedNeighbors = world.getLoadedNeighbors(chunk);

            dirtyChunks.addAll(loadedNeighbors);

            chunk.getLightingData().markAllDirty();
        }
    }
}