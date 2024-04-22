package app.util;

import util.Vector3iWrapper;

public interface IVec3i extends Comparable<IVec3i> {
    int x();
    int y();
    int z();
    void set(int x, int y, int z);

    interface ComponentWiseFunction {
        int apply(int component);
    }

    default <T extends IVec3i> T add(int x, int y, int z, T dest) {
        dest.set(x() + x, y() + y, z() + z);
        return dest;
    }
    default <T extends IVec3i> T add(IVec3i value, T dest) {
        return add(value.x(), value.y(), value.z(), dest);
    }

    default <T extends IVec3i> T sub(int x, int y, int z, T dest) {
        dest.set(x() - x, y() - y, z() - z);
        return dest;
    }
    default <T extends IVec3i> T sub(IVec3i value, T dest) {
        return sub(value.x(), value.y(), value.z(), dest);
    }

    default <T extends IVec3i> T mul(int value, T dest) {
        dest.set(x() * value, y() * value, z() * value);
        return dest;
    }

    default <T extends IVec3i> T div(int value, T dest) {
        dest.set(x() / value, y() / value, z() / value);
        return dest;
    }

    default <T extends IVec3i> T neg(T dest) {
        dest.set(-x(), -y(), -z());
        return dest;
    }

    default <T extends IVec3i> T all(ComponentWiseFunction func, T dest) {
        dest.set(func.apply(x()), func.apply(y()), func.apply(z()));
        return dest;
    }

    default int defaultHash() {
        int x = x(), y = y(), z = z();

        int result = x ^ (x >>> 16);
        result = 31 * result + (y ^ (y >>> 16));
        result = 31 * result + (z ^ (z >>> 16));
        return result;
    }

    @Override
    default int compareTo(IVec3i other) {
        int x1 = x(), x2 = other.x();
        if(x1 != x2) return Integer.compare(x1, x2);
        int y1 = y(), y2 = other.y();
        if(y1 != y2) return Integer.compare(y1, y2);
        int z1 = z(), z2 = other.z();
        return Integer.compare(z1, z2);
    }
}
