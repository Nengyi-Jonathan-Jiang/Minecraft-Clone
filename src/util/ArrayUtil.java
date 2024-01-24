package util;

import java.util.List;

public class ArrayUtil {
    private ArrayUtil() {}

    public static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
