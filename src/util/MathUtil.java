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
        Vector3i res = new Vector3i();
        for(var vec : vectors) res.add(vec);
        return res;
    }
}
