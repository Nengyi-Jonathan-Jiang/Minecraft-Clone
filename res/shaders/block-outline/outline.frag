#version 330

in vec3 outPosition;

out vec4 fragColor;

float f(float a, float b) {
    return abs(abs(a + b) + abs(a - b) - 1);
}

const float limit = 0.02;

void main()
{
    float x = outPosition.x;
    float y = outPosition.y;
    float z = outPosition.z;
    if(f(x, y) >= limit || f(x, z) >= limit || f(y, z) >= limit) discard;
    fragColor = vec4(0, 0, 0, 1);
}