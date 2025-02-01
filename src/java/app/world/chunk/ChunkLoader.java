package app.world.chunk;

import app.world.World;
import app.world.util.ChunkOffset;
import org.jetbrains.annotations.NotNull;
import util.Resource;
import util.TaskPool;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkLoader implements Resource {
    private final World world;
    private final Thread thread;

    private int nextWaitingChunkId;
    private final TaskPool<ChunkLoaderTask, Map<Integer, ChunkLoaderTask>> waitingChunks;

    public ChunkLoader(World world, Comparator<ChunkOffset> priority) {
        this.world = world;
        this.waitingChunks = new TaskPool<>(
            new HashMap<>(),
            (c, task) -> c.put(task.id, task),
            (c) -> {
                ChunkLoaderTask result = Collections.min(c.values(), (a, b) -> priority.compare(a.offset, b.offset));
                c.remove(result.id);
                return result;
            },
            Map::isEmpty
        );

        thread = new Thread(this::runChunkLoaderThread);
        thread.start();
    }

    private void runChunkLoaderThread() {
        while (true) {
            try {
                ChunkLoaderTask task = waitingChunks.take();
                doTask(task);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public ChunkLoaderTask requestChunk(ChunkOffset offset) {
        ChunkLoaderTask task = createChunkLoaderTask(offset);
        waitingChunks.offer(task);
        return task;
    }

    public ChunkLoaderTask requestChunkImmediate(ChunkOffset offset) {
        ChunkLoaderTask task = createChunkLoaderTask(offset);
        doTask(task);
        return task;
    }

    private void doTask(ChunkLoaderTask task) {
        Chunk chunk = world.getWorldGenerator().generateChunk(task.offset, world);
        task.result.set(chunk);
        world.getLightingEngine().invalidateLighting(List.of(chunk));
    }

    private @NotNull ChunkLoaderTask createChunkLoaderTask(ChunkOffset offset) {
        return new ChunkLoaderTask(
            nextWaitingChunkId++,
            offset,
            this
        );
    }

    @Override
    public void freeResources() {
        thread.interrupt();
    }

    public static class ChunkLoaderTask {
        private final int id;
        private final ChunkOffset offset;
        private final ChunkLoader loader;

        private final AtomicReference<Chunk> result = new AtomicReference<>();

        private ChunkLoaderTask(int id, ChunkOffset offset, ChunkLoader loader) {
            this.id = id;
            this.offset = offset;
            this.loader = loader;
        }

        public boolean hasResult() {
            return result.get() != null;
        }

        public Chunk get() {
            return result.get();
        }

        public void doImmediately() {
            cancel();
            loader.doTask(this);
        }

        public void cancel() {
            loader.waitingChunks.doOperation(c -> c.remove(id));
        }
    }
}