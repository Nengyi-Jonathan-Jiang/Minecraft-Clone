package util;

import java.util.Arrays;

public class DenseIntMap<T> {
    private int capacity = 256;
    @SuppressWarnings("unchecked")
    private T[] items = (T[]) new Object[capacity];

    public DenseIntMap() {}

    public T get(int key) {
        allocateSpace(key);
        return items[key];
    }

    public void put(int key, T value) {
        allocateSpace(key);
        items[key] = value;
    }

    private void allocateSpace(int key) {
        if (key >= capacity) {
            int targetCapacity = capacity;
            while (key >= targetCapacity) targetCapacity *= 2;
            items = Arrays.copyOf(items, targetCapacity);
            capacity = targetCapacity;
        }
    }
}
