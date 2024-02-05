package util;

import java.util.function.IntFunction;

public class ArrayConcatenator<T> {
    @SuppressWarnings("unchecked")
    private T[] arr = (T[]) new Object[16];
    private int capacity = 16;
    private int size = 0;

    public void addAll(T... items) {
        growBy(items.length);
        System.arraycopy(items, 0, arr, size - items.length, items.length);
    }

    public T[] get(IntFunction<T[]> createArray){
        T[] res = createArray.apply(size);
        System.arraycopy(arr, 0, res, 0, size);
        return res;
    }

    public void clear() {
        //noinspection unchecked
        arr = (T[]) new Object[16];
        capacity = 16;
        size = 0;
    }

    public int size() {
        return size;
    }

    private void growBy(int growSize) {
        size += growSize;
        if(size >= capacity) {
            int targetCapacity = capacity;
            while(size >= targetCapacity) targetCapacity *= 2;
            //noinspection unchecked
            T[] newArr = (T[]) new Object[targetCapacity];
            System.arraycopy(arr, 0, newArr, 0, capacity);
            arr = newArr;
            capacity = targetCapacity;
        }
    }
}
