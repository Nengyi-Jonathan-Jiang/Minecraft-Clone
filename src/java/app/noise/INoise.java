package app.noise;

public interface INoise {
    double getNoise(double x, double y, double z);
    float getNoise(float x, float y, float z);

    double getNoise(double x, double y);
    float getNoise(float x, float y);
}
