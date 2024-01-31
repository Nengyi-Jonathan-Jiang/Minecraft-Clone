package util;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class UniqueQueue<T> {
    private final Queue<T> backingQueue;
    private final Set<T> elements;


    public UniqueQueue() {
        backingQueue = new ArrayDeque<>();
        elements = new HashSet<>();
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
