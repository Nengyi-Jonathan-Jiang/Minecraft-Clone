#version 330

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 uv;
layout (location = 2) in vec4 lightLevels;                // Convention: (light(0,0), light(1,0), light(0,1), light(1,1))
layout (location = 3) in vec2 lightLevelInterpolator;

out vec2 outUV;
out vec4 outLightLevels;
out vec2 outLightLevelInterpolator;
out vec3 outRealWorldPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main()
{
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    outRealWorldPos = (viewMatrix * modelMatrix * vec4(position, 1.0)).xyz;
    outUV = uv;
    outLightLevels = lightLevels;
    outLightLevelInterpolator = lightLevelInterpolator;
}