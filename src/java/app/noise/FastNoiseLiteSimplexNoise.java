package app.noise;

import fastnoiselite.FastNoiseLite;

public class FastNoiseLiteSimplexNoise implements INoise {
    private final FastNoiseLite noise;

    public FastNoiseLiteSimplexNoise() {
        this((int)(Math.random() * (1 << 31)));
    }

    public FastNoiseLiteSimplexNoise(int seed) {
        this.noise = new FastNoiseLite(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.Value);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);

        noise.SetFractalOctaves(3);
        noise.SetFractalLacunarity(2f);
        noise.SetFractalGain(0.5f);
//        noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2Reduced);
//        noise.SetDomainWarpAmp(20.0f);
        noise.SetFrequency(1);
    }

    @Override
    public double getNoise(double x, double y, double z) {
        return getNoise((float) x, (float) y, (float) z);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return noise.GetNoise(x, y, z);
    }

    @Override
    public double getNoise(double x, double y) {
        return getNoise((float) x, (float) y);
    }

    @Override
    public float getNoise(float x, float y) {
        return noise.GetNoise(x, y);
    }
}
