package app.util;

public class PositionInChunk implements Vec3i {
    private int bits;

    public PositionInChunk(int x, int y, int z) {
        set(x, y, z);
    }

    @Override
    public int x() {
        return bits & 15;
    }

    @Override
    public int y() {
        return (bits >> 4) & 255;
    }

    @Override
    public int z() {
        return (bits >> 12) & 15;
    }

    @Override
    public void set(int x, int y, int z) {
        bits = (x & 15) | ((y & 255) << 4) | ((z & 15) << 12);
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public int compareTo(Vec3i other) {
        if(other instanceof PositionInChunk pos) {
            return bits - pos.bits;
        }
        return Vec3i.super.compareTo(other);
    }
}
