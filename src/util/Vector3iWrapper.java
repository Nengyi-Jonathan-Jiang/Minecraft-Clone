package util;

import org.joml.Vector2i;
import org.joml.Vector3i;

public class Vector3iWrapper extends Vector3i {
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

    private static class Dummy<T> {
        public T cast(T... x) {
            return x[0];
        }
    }
}
