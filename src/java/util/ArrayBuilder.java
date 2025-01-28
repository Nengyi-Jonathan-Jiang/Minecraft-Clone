package util;

/**
 * A class for efficiently building large arrays.
 */
@SuppressWarnings("SuspiciousSystemArraycopy")
public abstract class ArrayBuilder<T> {
    private T arr;
    private int capacity;
    private int size;

    {
        clear();
    }

    public static class IntArrayBuilder extends ArrayBuilder<int[]> {
        @Override
        protected int getLength(int[] arr) {
            return arr.length;
        }

        @Override
        protected int[] createArray(int length) {
            return new int[length];
        }

        @Override
        public void addAll(int... items) {
            super.addAll(items);
        }
    }

    public static class FloatArrayBuilder extends ArrayBuilder<float[]> {
        @Override
        protected int getLength(float[] arr) {
            return arr.length;
        }

        @Override
        protected float[] createArray(int length) {
            return new float[length];
        }

        @Override
        public void addAll(float... items) {
            super.addAll(items);
        }
    }

    protected abstract int getLength(T arr);

    protected abstract T createArray(int length);

    public void addAll(T items) {
        int numItems = getLength(items);
        growBy(numItems);
        System.arraycopy(items, 0, arr, size - numItems, numItems);
    }

    public T get() {
        T res = createArray(size);
        System.arraycopy(arr, 0, res, 0, size);
        return res;
    }

    public void clear() {
        clear(16);
    }

    public void clear(int newCapacity) {
        if (newCapacity <= 0) throw new IllegalArgumentException("Capacity of ArrayBuilder must be at least 1");
        arr = createArray(newCapacity);
        capacity = newCapacity;
        size = 0;
    }

    public int size() {
        return size;
    }

    public void growToCapacity(int capacity) {
        if (capacity >= this.capacity) {
            int targetCapacity = this.capacity;
            while (capacity >= targetCapacity) targetCapacity *= 2;
            T newArr = createArray(targetCapacity);
            System.arraycopy(arr, 0, newArr, 0, this.capacity);
            arr = newArr;
            this.capacity = targetCapacity;
        }
    }

    private void growBy(int growSize) {
        size += growSize;
        growToCapacity(size);
    }
}
