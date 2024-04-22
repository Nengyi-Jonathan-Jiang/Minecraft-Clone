package util;


public class Vector3iWrapper extends WorldPosition {
    public Vector3iWrapper() { super(); }
    public Vector3iWrapper(WorldPosition value) { super(value); }
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
}