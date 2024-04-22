package app;

import org.joml.Vector3i;
import util.Vector3iWrapper;

public class SmallVec3i implements Comparable<SmallVec3i> {
    private final int bits;
    public SmallVec3i(Vector3i v) {
        this(v.x, v.y, v.z);
    }
    public SmallVec3i(int x, int y, int z) {
        this.bits = (x & 15) + ((y & 255) << 4) + ((z & 15) << 12);
    }
    public SmallVec3i(int bits) {
        this.bits = bits;
    }

    public int x() {
        return bits & 15;
    }
    public int y() {
        return (bits >> 4) & 255;
    }
    public int z() {
        return (bits >> 12) & 15;
    }

    @Override
    public int hashCode() {
        return bits;
    }

    @Override
    public int compareTo(SmallVec3i o) {
        return Integer.compare(bits, o.bits);
    }

    public Vector3iWrapper toVec3i() {
        return new Vector3iWrapper(x(), y(), z());
    }

    public int getBits() {
        return bits;
    }
}
