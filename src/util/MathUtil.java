package util;

import org.apache.commons.math3.util.FastMath;
import org.joml.Vector3i;

import java.util.Arrays;

public class MathUtil {
    private MathUtil() {}

    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    public static Vector3i sum(Vector3i... vectors) {
        return Arrays.stream(vectors).reduce(new Vector3i(), (a, b) -> a.add(b, new Vector3i()));
    }
}
