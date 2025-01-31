#version 430

in vec2 outUV;
out vec4 fragColor;

#include "shaders/lib/simplex_noise.glsl"

uniform sampler2D txtSampler;

void main() {
    float color = simplex_noise(outUV.xy * 40.) * 0.5 + 0.5;
    fragColor = vec4(vec3(color), 1.0);
}