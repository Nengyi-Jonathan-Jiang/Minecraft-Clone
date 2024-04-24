package util;

import java.util.*;
import java.util.function.Supplier;

public class UniqueQueue<T> {
    private final Queue<T> backingQueue;
    private final Set<T> elements;

    public UniqueQueue() {
        this(HashSet::new);
    }

    public UniqueQueue(Supplier<Set<T>> containerSupplier) {
        backingQueue = new ArrayDeque<>();
        elements = containerSupplier.get();
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
