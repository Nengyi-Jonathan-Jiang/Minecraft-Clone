#version 330

in vec2 outUV;
in vec4 outLightLevels;
in vec2 outLightLevelInterpolator;

out vec4 fragColor;

uniform sampler2D txtSampler;

float cubic_interpolation(float t) {
    return 2 * t * (0.75 - t * t);
}

float interpolate(float a, float b, float t) {
    return (a + b) / 2 + (b - a) * cubic_interpolation(t);
}

const bool whiteWorld = true;

void main()
{
    float lightLevel = interpolate(
        interpolate(outLightLevels.x, outLightLevels.y, outLightLevelInterpolator.x),
        interpolate(outLightLevels.z, outLightLevels.w, outLightLevelInterpolator.x),
        outLightLevelInterpolator.y
    );

    if(whiteWorld) {
        fragColor = texture(txtSampler, outUV) * 0.0000000001 + lightLevel;
    }
    else {
        fragColor = texture(txtSampler, outUV) * (lightLevel * 0.8 + 0.2);
    }
}