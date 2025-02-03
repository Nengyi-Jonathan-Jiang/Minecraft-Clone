#version 430

in vec2 outUV;
out vec4 fragColor;

#include "shaders/lib/simplex_noise.glsl"

void main() {
    float scale = 0.2;
    int octaves = 8;
    float color = 0.0;
    for(int i = 0; i < octaves; i++) {
        color += simplex_noise(outUV.xy * pow(2.0, float(i)) / scale) * pow(2.0, -float(i));
    }
    color = color * (pow(2.0, octaves) / (pow(2.0, octaves) - 1.0)) * 0.25 + 0.5;
    fragColor = vec4(vec3(color), 1.0);
}