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
        arr = createArray(16);
        capacity = 16;
        size = 0;
    }

    public int size() {
        return size;
    }

    private void growBy(int growSize) {
        size += growSize;
        if (size >= capacity) {
            int targetCapacity = capacity;
            while (size >= targetCapacity) targetCapacity *= 2;
            T newArr = createArray(targetCapacity);
            System.arraycopy(arr, 0, newArr, 0, capacity);
            arr = newArr;
            capacity = targetCapacity;
        }
    }
}
