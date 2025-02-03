#version 430

layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

uniform ivec3 chunkOffset;

layout (std430, binding = 0) writeonly buffer result {
    uint blockIDs[65536];
};

#include "shaders/lib/simplex_noise.glsl"

void main() {
    uint result = 0;
    uvec3 positionInChunk = gl_GlobalInvocationID.xyz;
    ivec3 worldPos = chunkOffset + ivec3(positionInChunk);

    float height = simplex_noise(vec2(worldPos.xz) * 0.02) * 10.0 + 64.0;
    if (worldPos.y < height - 3) {
        result = 3;
    }
    else if (worldPos.y < height) {
        result = 1;
    }

    uint writeIndex = positionInChunk.x + (positionInChunk.z << 4) + (positionInChunk.y << 8);
    blockIDs[writeIndex] = result;
}