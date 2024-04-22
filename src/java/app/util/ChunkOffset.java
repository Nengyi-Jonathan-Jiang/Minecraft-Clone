package app.util;

public class ChunkOffset implements IVec3i {
    private int x;
    private int z;

    public ChunkOffset() {
        this(0, 0);
    }

    public ChunkOffset(int x, int z) {
        set(x, 0, z);
    }

    @Override
    public int x() {
        return x;
    }

    @Deprecated
    @Override
    public int y() {
        return 0;
    }

    @Override
    public int z() {
        return z;
    }

    @Override
    public void set(int x, int y, int z) {
        this.x = x & ~15;
        this.z = z & ~15;
    }

    @Override
    public int hashCode() {
        return defaultHash();
    }

    public static ChunkOffset fromAbsolutePosition(WorldPosition pos) {
        return new ChunkOffset(pos.x(), pos.z());
    }
}
