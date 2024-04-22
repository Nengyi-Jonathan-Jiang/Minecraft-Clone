package util;

import org.joml.Vector3i;

public class Vector3iWrapper extends Vector3i implements Comparable<Vector3iWrapper> {
    public Vector3iWrapper() { super(); }
    public Vector3iWrapper(Vector3i value) { super(value); }
    public Vector3iWrapper(int x, int y, int z) { super(x, y, z); }

    @Override
    public int hashCode() {
        int result = x ^ (x >>> 16);
        result = 31 * result + (y ^ (y >>> 16));
        result = 31 * result + (z ^ (z >>> 16));
        return result;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        Vector3iWrapper vec = (Vector3iWrapper) obj;
        return vec.x == x && vec.y == y && vec.z == z;
    }

    @Override
    public int compareTo(Vector3iWrapper o) {
        int r1 = Integer.compare(x, o.x);
        if(r1 != 0) return r1;
        int r2 = Integer.compare(y, o.y);
        if(r2 != 0) return r2;
        else return Integer.compare(z, o.z);
    }
}