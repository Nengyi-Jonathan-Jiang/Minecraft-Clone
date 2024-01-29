#version 330

in vec2 outUV;
in vec4 outLightLevels;
in vec2 outLightLevelInterpolator;

out vec4 fragColor;

uniform sampler2D txtSampler;


float lerp(float a, float b, float t) {
    return (a + b) / 2 + (b - a) * t;
}

const bool whiteWorld = false;

void main()
{
    float lightLevel = lerp(
        lerp(outLightLevels.x, outLightLevels.y, outLightLevelInterpolator.x),
        lerp(outLightLevels.z, outLightLevels.w, outLightLevelInterpolator.x),
        outLightLevelInterpolator.y
    );

    if(whiteWorld) {
        fragColor = texture(txtSampler, outUV) * 0.0000000001 + lightLevel;
    }
    else {
        fragColor = texture(txtSampler, outUV) * (lightLevel * 0.8 + 0.2);
    }
}