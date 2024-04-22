package util;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;

public class UniqueQueue<T> {
    private final Queue<T> backingQueue;
    private final Set<T> elements;

    public UniqueQueue() {
        this(HashSet::new);
    }

    public UniqueQueue(Supplier<Set<T>> container) {
        backingQueue = new ArrayDeque<>();
        elements = container.get();
    }

    public void offer(T element) {
        if(elements.add(element)) backingQueue.offer(element);
    }

    public boolean isEmpty() {
        return backingQueue.isEmpty();
    }

    public T poll() {
        T result = backingQueue.poll();
        elements.remove(result);
        return result;
    }
}
