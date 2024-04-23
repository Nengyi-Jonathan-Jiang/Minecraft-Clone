package app.util;

public class Vec3i implements IVec3i {
    private int x, y ,z;

    public Vec3i() {
        this(0, 0, 0);
    }

    public Vec3i(int x, int y, int z) {
        set(x, y, z);
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return defaultHash();
    }

    @Override
    public String toString() {
        return defaultToString();
    }

    @Override
    public boolean equals(Object obj) {
        return defaultEquals(obj);
    }
}
