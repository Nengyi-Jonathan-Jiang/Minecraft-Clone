package app.util;

public interface Vec2i extends Comparable<Vec2i> {
    int x();
    int y();
    void set(int x, int y);

    interface ComponentWiseFunction {
        int apply(int component);
    }

    default Vec2i add(int x, int y, Vec2i dest) {
        dest.set(x() + x, y() + y);
        return dest;
    }
    default Vec2i add(Vec2i value, Vec2i dest) {
        return add(value.x(), value.y(), dest);
    }

    default Vec2i sub(int x, int y, Vec2i dest) {
        dest.set(x() - x, y() - y);
        return dest;
    }
    default Vec2i sub(Vec2i value, Vec2i dest) {
        return sub(value.x(), value.y(), dest);
    }

    default Vec2i mul(int value, Vec2i dest) {
        dest.set(x() * value, y() * value);
        return dest;
    }

    default Vec2i div(int value, Vec2i dest) {
        dest.set(x() / value, y() / value);
        return dest;
    }

    default Vec2i neg(Vec2i dest) {
        dest.set(-x(), -y());
        return dest;
    }

    default Vec2i all(ComponentWiseFunction func, Vec2i dest) {
        dest.set(func.apply(x()), func.apply(y()));
        return dest;
    }

    default int defaultHash() {
        int x = x(), y = y();

        int result = x ^ (x >>> 16);
        result = 31 * result + (y ^ (y >>> 16));
        return result;
    }

    @Override
    default int compareTo(Vec2i other) {
        int x1 = x(), x2 = other.x();
        if(x1 != x2) return Integer.compare(x1, x2);
        int y1 = y(), y2 = other.y();
        return Integer.compare(y1, y2);
    }
}
