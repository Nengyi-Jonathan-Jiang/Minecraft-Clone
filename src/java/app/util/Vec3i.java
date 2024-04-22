package app.util;

public interface Vec3i extends Comparable<Vec3i> {
    int x();
    int y();
    int z();
    void set(int x, int y, int z);

    interface ComponentWiseFunction {
        int apply(int component);
    }

    default Vec3i add(int x, int y, int z, Vec3i dest) {
        dest.set(x() + x, y() + y, z() + z);
        return dest;
    }
    default Vec3i add(Vec3i value, Vec3i dest) {
        return add(value.x(), value.y(), value.z(), dest);
    }

    default Vec3i sub(int x, int y, int z, Vec3i dest) {
        dest.set(x() - x, y() - y, z() - z);
        return dest;
    }
    default Vec3i sub(Vec3i value, Vec3i dest) {
        return sub(value.x(), value.y(), value.z(), dest);
    }

    default Vec3i mul(int value, Vec3i dest) {
        dest.set(x() * value, y() * value, z() * value);
        return dest;
    }

    default Vec3i div(int value, Vec3i dest) {
        dest.set(x() / value, y() / value, z() / value);
        return dest;
    }

    default Vec3i neg(Vec3i dest) {
        dest.set(-x(), -y(), -z());
        return dest;
    }

    default Vec3i all(ComponentWiseFunction func, Vec3i dest) {
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
    default int compareTo(Vec3i other) {
        int x1 = x(), x2 = other.x();
        if(x1 != x2) return Integer.compare(x1, x2);
        int y1 = y(), y2 = other.y();
        if(y1 != y2) return Integer.compare(y1, y2);
        int z1 = z(), z2 = other.z();
        return Integer.compare(z1, z2);
    }
}
