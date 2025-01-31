#version 430

in vec2 outUV;
in vec4 outLightLevels;
in vec2 outLightLevelInterpolator;
in vec3 outRealWorldPos;

out vec4 fragColor;

uniform sampler2D txtSampler;

float cubic_interpolation(float t) {
    return 2 * t * (0.75 - t * t);
}

float clamped_cubic_interpolation(float t) {
    return t < -.5 ? 0 : t > .5 ? 1 : cubic_interpolation(t) + 0.5;
}

float interpolate(float a, float b, float t) {
    return (a + b) / 2 + (b - a) * cubic_interpolation(t);
}

vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}

const bool whiteWorld = false;
const vec4 fogColor = vec4(.7, .93, 1., 1.);
const float fogStrength = 0.02;

void main()
{
    float lightLevel = interpolate(
        interpolate(outLightLevels.x, outLightLevels.y, outLightLevelInterpolator.x),
        interpolate(outLightLevels.z, outLightLevels.w, outLightLevelInterpolator.x),
        outLightLevelInterpolator.y
    );

    // Apply lighting and texture
    if (whiteWorld) {
        fragColor = texture(txtSampler, outUV) * 0.0000000001 + lightLevel;
    }
    else {
        fragColor = texture(txtSampler, outUV) * (lightLevel * lightLevel * 0.8 + 0.2);
    }

    // Apply fog
    fragColor = lerp(fragColor, fogColor, 1. - clamped_cubic_interpolation((64. - length(outRealWorldPos)) / 40.));
}