package app.world.chunk;

import app.world.World;
import app.world.util.ChunkOffset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Resource;

import java.util.*;

public class ChunkLoader implements Resource {
    private final World world;
    private final Thread thread;
    private final Comparator<ChunkOffset> priority;

    private int nextWaitingChunkId;
    private final Map<Integer, ChunkLoaderTask> waitingChunks;

    public ChunkLoader(World world, Comparator<ChunkOffset> priority) {
        this.world = world;
        this.waitingChunks = new HashMap<>();
        this.priority = priority;

        thread = new Thread(this::runChunkLoaderThread);
        thread.start();
    }

    private void runChunkLoaderThread() {
        while (true) {
            // Need this to catch interrupts in main thread.
            try {
                //noinspection BusyWait
                Thread.sleep(0);
            } catch (InterruptedException e) {
                break;
            }

            ChunkLoaderTask task;
            synchronized (waitingChunks) {
                Optional<ChunkLoaderTask> optionalTask = waitingChunks.values().stream().min((a, b) -> priority.compare(a.offset, b.offset));
                if (optionalTask.isEmpty()) {
                    continue;
                }
                task = optionalTask.get();
                waitingChunks.remove(task.id);
            }

            synchronized (task) {
                doTask(task);
            }
        }
    }

    public ChunkLoaderTask requestChunk(ChunkOffset offset) {
        ChunkLoaderTask task = createChunkLoaderTask(offset);
        synchronized (waitingChunks) {
            waitingChunks.put(task.id, task);
        }
        return task;
    }

    public ChunkLoaderTask requestChunkImmediate(ChunkOffset offset) {
        ChunkLoaderTask task = createChunkLoaderTask(offset);
        doTask(task);
        return task;
    }

    private void doTask(ChunkLoaderTask task) {
        Chunk chunk = task.result = world.getWorldGenerator().generateChunk(task.offset, world);
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

        private @Nullable Chunk result;

        private ChunkLoaderTask(int id, ChunkOffset offset, ChunkLoader loader) {
            this.id = id;
            this.offset = offset;
            this.loader = loader;
        }

        public boolean hasResult() {
            return result != null;
        }

        public @Nullable Chunk get() {
            return result;
        }

        public void doImmediately() {
            cancel();
            loader.doTask(this);
        }

        public void cancel() {
            synchronized (loader.waitingChunks) {
                loader.waitingChunks.remove(id);
            }
        }
    }
}
