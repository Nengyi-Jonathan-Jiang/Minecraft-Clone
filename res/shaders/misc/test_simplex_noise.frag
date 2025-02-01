#version 430

in vec2 outUV;
out vec4 fragColor;

#include "shaders/lib/simplex_noise.glsl"

void main() {
    float color = (
        simplex_noise(outUV.xy * 5.) * (8.0 / 15.0)
        + simplex_noise(outUV.xy * 10.) * (4.0 / 15.0)
        + simplex_noise(outUV.xy * 20.) * (2.0 / 15.0)
        + simplex_noise(outUV.xy * 40.) * (1.0 / 15.0)
    ) * 0.5 + 0.5;
    fragColor = vec4(vec3(mod(color, 1.0)), 1.0);
}