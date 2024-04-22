package app.util;

public class ChunkOffset implements Vec3i {
    private int x;
    private int y;
    private int z;

    public ChunkOffset(int x, int y, int z) {
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
}
