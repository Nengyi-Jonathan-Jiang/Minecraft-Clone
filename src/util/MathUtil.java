package util;

public class MathUtil {
    private MathUtil() {}

    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    public static int mod(int x, int m) {
        return (x % m + m) % m;
    }
}
