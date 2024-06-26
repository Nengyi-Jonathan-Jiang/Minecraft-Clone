package app.world.util;

public class PositionInChunk implements IVec3i {
    private int bits;

    public PositionInChunk(int x, int y, int z) {
        set(x, y, z);
    }

    public PositionInChunk(int bits) {
        this.bits = bits;
    }

    public PositionInChunk() {
        this.bits = 0;
    }

    @Override
    public int x() {
        return bits & 15;
    }

    @Override
    public int y() {
        return bits >> 8;
    }

    @Override
    public int z() {
        return (bits >> 4) & 15;
    }

    @Override
    public void set(int x, int y, int z) {
        bits = (x & 15) | ((z & 15) << 4) | (y << 8);
    }

    public WorldPosition getAbsolutePosition(ChunkOffset chunkOffset) {
        return chunkOffset.add(this, new WorldPosition());
    }

    public int getBits() {
        return bits;
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public int compareTo(IVec3i other) {
        if(other instanceof PositionInChunk pos) {
            return bits - pos.bits;
        }
        return IVec3i.super.compareTo(other);
    }

    @Override
    public String toString() {
        return "PositionInChunk" + defaultToString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PositionInChunk pos) {
            return bits == pos.bits;
        }
        return defaultEquals(obj);
    }
}
