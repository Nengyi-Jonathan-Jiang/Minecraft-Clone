package app.noise;

import org.joml.SimplexNoise;

public class JomlSimplexNoise implements INoise {
    @Override
    public double getNoise(double x, double y, double z) {
        return getNoise((float) x, (float) y, (float) z);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return SimplexNoise.noise(x, y, z);
    }

    @Override
    public double getNoise(double x, double y) {
        return getNoise((float) x, (float) y);
    }

    @Override
    public float getNoise(float x, float y) {
        return SimplexNoise.noise(x, y);
    }
}
