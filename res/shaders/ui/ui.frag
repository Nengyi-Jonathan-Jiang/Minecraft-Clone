#version 330

in vec2 outUV;
out vec4 fragColor;

uniform sampler2D txtSampler;

void main() {
    vec4 color = texture(txtSampler, outUV);
    if(color.a <= 0.01) discard;
    fragColor = color;
}