package app.world.util;

import java.util.Iterator;
import java.util.function.Consumer;

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

    public static void forAllPositionsInChunk(Consumer<PositionInChunk> action) {
        PositionInChunk pos = new PositionInChunk();
        for (int bits = 0; bits < 0x10000; bits++) {
            pos.bits = bits;
            action.accept(pos);
        }
    }

    public static Iterable<PositionInChunk> allPositionsInChunk() {
        return () -> new Iterator<>() {
            final PositionInChunk current = new PositionInChunk(-1);

            @Override
            public boolean hasNext() {
                return current.getBits() != 0xffff;
            }

            @Override
            public PositionInChunk next() {
                current.bits++;
                return current;
            }
        };
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

    public WorldPosition getWorldPosition(ChunkOffset chunkOffset) {
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
        if (other instanceof PositionInChunk pos) {
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
        if (obj instanceof PositionInChunk pos) {
            return bits == pos.bits;
        }
        return defaultEquals(obj);
    }
}
