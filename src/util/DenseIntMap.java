package util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DenseIntMap<T> {
    @SuppressWarnings("unchecked")
    private T[] items = (T[]) new Object[16];
    private int capacity = 16;

    public DenseIntMap() {}

    public T get(int key) {
        allocateSpace(key);
        return items[key];
    }

    public void put(int key, T value) {
        items[key] = value;
    }

    private void allocateSpace(int key) {
        if(key >= capacity) {
            int targetCapacity = capacity;
            while(key >= targetCapacity) targetCapacity *= 2;
            //noinspection unchecked
            T[] newItems = (T[]) new Object[targetCapacity];
            System.arraycopy(items, 0, newItems, 0, capacity);
            items = newItems;
            capacity = targetCapacity;
        }
    }

}
