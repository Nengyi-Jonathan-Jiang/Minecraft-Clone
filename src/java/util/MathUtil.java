package util;


import app.util.IVec3i;
import app.util.Vec3i;

import java.util.Arrays;

public class MathUtil {
    private MathUtil() {}

    public static float mod(float x, float m) {
        return (x % m + m) % m;
    }

    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    public static <T extends IVec3i> T addAll(T dest, Vec3i... vectors) {
        for(var vec : vectors) dest.add(vec, dest);
        return dest;
    }

    public static float min(double... x) {
        //noinspection OptionalGetWithoutIsPresent
        return (float) Arrays.stream(x).min().getAsDouble();
    }
}
