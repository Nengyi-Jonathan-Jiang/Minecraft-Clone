package app.world.lighting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class DirtyInts implements Set<Integer> {
    private final int[] nextValue;
    private int lastValue;
    private final int capacity;
    private int count;

    private static final int EMPTY_VALUE = -2;
    private static final int NO_NEXT_VALUE = -1;

    public DirtyInts(int capacity) {
        this.capacity = capacity;

        this.nextValue = new int[capacity + 1];
        clear();
    }

    public boolean isDirty(int i) {
        return nextValue[i] != EMPTY_VALUE;
    }

    public boolean markDirty(int i) {
        if(nextValue[i] != EMPTY_VALUE) return false;

        this.nextValue[this.lastValue] = i;

        this.nextValue[this.lastValue = i] = NO_NEXT_VALUE;
        this.count++;

        return true;
    }

    public void markAllDirty() {
        nextValue[capacity] = 0;
        for(int i = 0; i < capacity - 1; i++) {
            nextValue[i] = i + 1;
        }
        nextValue[capacity - 1] = NO_NEXT_VALUE;
    }

    public int count() {
        return count;
    }

    @Override
    public void clear() {
        Arrays.fill(nextValue, EMPTY_VALUE);
        this.nextValue[capacity] = NO_NEXT_VALUE;
        this.lastValue = capacity;
        this.count = 0;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public int size() {
        return count();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            return isDirty((int) o);
        }
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int value = nextValue[capacity];

            @Override
            public boolean hasNext() {
                return value != NO_NEXT_VALUE;
            }

            @Override
            public Integer next() {
                int res = value;
                value = nextValue[value];
                return res;
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Integer i) {
        return markDirty(i);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        boolean res = false;
        for(Integer i : c) {
            res |= add(i);
        }
        return res;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        StringBuilder res = new StringBuilder("{");
        for(int i : this) {
            res.append(i).append(", ");
        }
        return res.append("}").toString();
    }
}
