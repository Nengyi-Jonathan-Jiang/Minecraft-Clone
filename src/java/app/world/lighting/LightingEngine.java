package app.world.lighting;

import app.block.BlockRegistry;
import app.block.model.BlockModel.FaceDirection;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.lighting.LightingUpdateQueue.LightingUpdateChunk;
import app.world.util.ChunkOffset;
import app.world.util.Vec3i;
import app.world.util.WorldPosition;

import java.util.*;

public class LightingEngine {

    private final World world;
    private final Map<ChunkOffset, LightingUpdateChunk> dirtyChunks = new HashMap<>();

    public LightingEngine(World world) {
        this.world = world;
    }

    public synchronized void markChunkAsDirty(Chunk c) {
        LightingUpdateChunk chunk = dirtyChunks.computeIfAbsent(
            c.getChunkOffset(),
            (__) -> new LightingUpdateChunk(c)
        );
    }

    public synchronized boolean needsUpdate() {
//        return false;
        return !dirtyChunks.isEmpty();
    }

    public synchronized void updateLighting() {

        if (!needsUpdate()) return;

        dirtyChunks
            .values().stream().toList()
            .stream()
            .map(LightingUpdateChunk::getChunk)
            .map(world::getLoadedNeighbors)
            .flatMap(Collection::stream)
            .forEach(this::markChunkAsDirty);

        LightingUpdateQueue lightingUpdates = new LightingUpdateQueue(dirtyChunks.values());

        System.out.println("Updating " + lightingUpdates.size() + " blocks in " + dirtyChunks.size() + " chunks");

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

        dirtyChunks.values().stream().map(LightingUpdateChunk::getChunk).forEach(Chunk::invalidateMesh);
        dirtyChunks.values().stream().map(LightingUpdateChunk::getChunk).map(Chunk::getLightingData).forEach(LightingData::clearDirtyBlocks);

        dirtyChunks.clear();
    }

    private void updateLightingStep(LightingUpdateQueue lightingUpdates) {
        var update = lightingUpdates.poll();
        WorldPosition pos = update.worldPosition();

        LightingUpdateChunk lightingChunk = update.containingChunk();
        Chunk chunk = lightingChunk.getChunk();

        int oldBlockLight = chunk.getLightingData().getBlockLightAt(pos.getPositionInChunk());

        int newBlockLight = 0;
        List<WorldPosition> neighbors = new ArrayList<>();

        int blockIDAtPos = chunk.getBlockIDAt(pos.getPositionInChunk());
        int opacity = blockIDAtPos == 0 ? 0 : BlockRegistry.getBlock(blockIDAtPos).opacity;

        if (pos.y() == World.CHUNK_HEIGHT - 1) newBlockLight = Math.max(15 - opacity, 0);

        for (var face : FaceDirection.OUTER_FACES) {
            Vec3i offset = face.direction;
            WorldPosition newPos = pos.add(offset, new WorldPosition());

            Chunk neighborChunk = lightingUpdates.getChunkAtPos(newPos);
            if (neighborChunk == null) continue;

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

    public synchronized void invalidateLighting(Collection<Chunk> chunksToUpdate) {
        for (Chunk chunk : chunksToUpdate) {
            Collection<Chunk> loadedNeighbors = world.getLoadedNeighbors(chunk);

            loadedNeighbors.forEach(c -> dirtyChunks.computeIfAbsent(c.getChunkOffset(), (__) -> new LightingUpdateChunk(c)));

            chunk.getLightingData().invalidateAll();
        }
    }
}