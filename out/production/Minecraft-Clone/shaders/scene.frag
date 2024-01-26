#version 330

in vec2 outUV;
in vec4 outLightLevels;
in vec2 outLightLevelInterpolator;

out vec4 fragColor;

uniform sampler2D txtSampler;


float lerp(float a, float b, float t) {
    return a + (b - a) * t;
}


void main()
{
    float lightLevel = lerp(
        lerp(outLightLevels.x, outLightLevels.y, outLightLevelInterpolator.x),
        lerp(outLightLevels.z, outLightLevels.w, outLightLevelInterpolator.x),
        outLightLevelInterpolator.y
    );

    fragColor = texture(txtSampler, outUV) * (lightLevel * 0.8 + 0.2);
}