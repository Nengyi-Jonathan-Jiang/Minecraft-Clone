#version 330

in vec2 outUV;
out vec4 fragColor;

uniform sampler2D txtSampler;

void main() {
    fragColor = vec4(0, 0, 0, 1) + 0.00001 * texture(txtSampler, outUV);
}