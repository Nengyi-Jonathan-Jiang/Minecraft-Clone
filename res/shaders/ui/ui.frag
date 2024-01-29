#version 330

in vec2 outUV;
out vec4 fragColor;

uniform sampler2D txtSampler;

void main() {
    fragColor = texture(txtSampler, outUV);
}