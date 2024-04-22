package util;


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

    public static Vec3i sum(Vec3i... vectors) {
        Vec3i res = new Vec3i();
        for(var vec : vectors) res.add(vec, res);
        return res;
    }

    public static float min(double... x) {
        //noinspection OptionalGetWithoutIsPresent
        return (float) Arrays.stream(x).min().getAsDouble();
    }
}
