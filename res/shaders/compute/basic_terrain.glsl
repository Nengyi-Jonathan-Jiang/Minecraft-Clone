#version 430

layout (local_size_x = 1, local_size_y = 1, local_size_z = 1) in;

uniform ivec3 chunkOffset;
layout (std430, binding = 0) writeonly buffer result {
    uint blockIDs[65536];
};

float simplex_noise(vec2 v);
float simplex_noise(vec3 v);

void main() {
    uint result = 0;
    ivec3 worldPos = ivec3(gl_WorkGroupID.xyz) + chunkOffset;

    float height = simplex_noise(vec2(worldPos.xy) * 0.02) * 30.0 + 64.0;
    if (worldPos.y < height - 3) {
        result = 2;
    }

    else if (worldPos.y < height) {
        result = 1;
    }

    uint writeIndex = worldPos.x + (worldPos.z << 4) + (worldPos.y << 8);
    blockIDs[writeIndex] = result;
}

const uvec3 grad3[12] = uvec3[12](
    uvec3(1, 1, 0), uvec3(-1, 1, 0), uvec3(1, -1, 0), uvec3(-1, -1, 0), uvec3(1, 0, 1), uvec3(-1, 0, 1), uvec3(1, 0, -1), uvec3(-1, 0, -1), uvec3(0, 1, 1), uvec3(0, -1, 1), uvec3(0, 1, -1), uvec3(0, -1, -1)
);
const uint perm[512] = uint[512](
    -105, -96, -119, 91, 90, 15, -125, 13, -55, 95, 96, 53, -62, -23, 7, -31, -116, 36, 103, 30, 69, -114, 8, 99, 37, -16, 21, 10, 23, -66, 6, -108, -9, 120, -22, 75, 0, 26, -59, 62, 94, -4, -37, -53, 117, 35, 11, 32, 57, -79, 33, 88, -19, -107, 56, 87, -82, 20, 125, -120, -85, -88, 68, -81, 74, -91, 71, -122, -117, 48, 27, -90, 77, -110, -98, -25, 83, 111, -27, 122, 60, -45, -123, -26, -36, 105, 92, 41, 55, 46, -11, 40, -12, 102, -113, 54, 65, 25, 63, -95, 1, -40, 80, 73, -47, 76, -124, -69, -48, 89, 18, -87, -56, -60, -121, -126, 116, -68, -97, 86, -92, 100, 109, -58, -83, -70, 3, 64, 52, -39, -30, -6, 124, 123, 5, -54, 38, -109, 118, 126, -1, 82, 85, -44, -49, -50, 59, -29, 47, 16, 58, 17, -74, -67, 28, 42, -33, -73, -86, -43, 119, -8, -104, 2, 44, -102, -93, 70, -35, -103, 101, -101, -89, 43, -84, 9, -127, 22, 39, -3, 19, 98, 108, 110, 79, 113, -32, -24, -78, -71, 112, 104, -38, -10, 97, -28, -5, 34, -14, -63, -18, -46, -112, 12, -65, -77, -94, -15, 81, 51, -111, -21, -7, 14, -17, 107, 49, -64, -42, 31, -75, -57, 106, -99, -72, 84, -52, -80, 115, 121, 50, 45, 127, 4, -106, -2, -118, -20, -51, 93, -34, 114, 67, 29, 24, 72, -13, -115, -128, -61, 78, 66, -41, 61, -100, -76, -105, -96, -119, 91, 90, 15, -125, 13, -55, 95, 96, 53, -62, -23, 7, -31, -116, 36, 103, 30, 69, -114, 8, 99, 37, -16, 21, 10, 23, -66, 6, -108, -9, 120, -22, 75, 0, 26, -59, 62, 94, -4, -37, -53, 117, 35, 11, 32, 57, -79, 33, 88, -19, -107, 56, 87, -82, 20, 125, -120, -85, -88, 68, -81, 74, -91, 71, -122, -117, 48, 27, -90, 77, -110, -98, -25, 83, 111, -27, 122, 60, -45, -123, -26, -36, 105, 92, 41, 55, 46, -11, 40, -12, 102, -113, 54, 65, 25, 63, -95, 1, -40, 80, 73, -47, 76, -124, -69, -48, 89, 18, -87, -56, -60, -121, -126, 116, -68, -97, 86, -92, 100, 109, -58, -83, -70, 3, 64, 52, -39, -30, -6, 124, 123, 5, -54, 38, -109, 118, 126, -1, 82, 85, -44, -49, -50, 59, -29, 47, 16, 58, 17, -74, -67, 28, 42, -33, -73, -86, -43, 119, -8, -104, 2, 44, -102, -93, 70, -35, -103, 101, -101, -89, 43, -84, 9, -127, 22, 39, -3, 19, 98, 108, 110, 79, 113, -32, -24, -78, -71, 112, 104, -38, -10, 97, -28, -5, 34, -14, -63, -18, -46, -112, 12, -65, -77, -94, -15, 81, 51, -111, -21, -7, 14, -17, 107, 49, -64, -42, 31, -75, -57, 106, -99, -72, 84, -52, -80, 115, 121, 50, 45, 127, 4, -106, -2, -118, -20, -51, 93, -34, 114, 67, 29, 24, 72, -13, -115, -128, -61, 78, 66, -41, 61, -100, -76
);
const uint permMod12[512] = uint[512](
    7, 4, 5, 7, 6, 3, 11, 1, 9, 11, 0, 5, 2, 5, 7, 9, 8, 0, 7, 6, 9, 10, 8, 3, 1, 0, 9, 10, 11, 10, 6, 4, 7, 0, 6, 3, 0, 2, 5, 2, 10, 0, 3, 11, 9, 11, 11, 8, 9, 9, 9, 4, 9, 5, 8, 3, 6, 8, 5, 4, 3, 0, 8, 7, 2, 9, 11, 2, 7, 0, 3, 10, 5, 2, 2, 3, 11, 3, 1, 2, 0, 7, 1, 2, 4, 9, 8, 5, 7, 10, 5, 4, 4, 6, 11, 6, 5, 1, 3, 5, 1, 0, 8, 1, 5, 4, 0, 7, 4, 5, 6, 1, 8, 4, 3, 10, 8, 8, 3, 2, 8, 4, 1, 6, 5, 6, 3, 4, 4, 1, 10, 10, 4, 3, 5, 10, 2, 3, 10, 6, 3, 10, 1, 8, 3, 2, 11, 11, 11, 4, 10, 5, 2, 9, 4, 6, 7, 3, 2, 9, 11, 8, 8, 2, 8, 10, 7, 10, 5, 9, 5, 11, 11, 7, 4, 9, 9, 10, 3, 1, 7, 2, 0, 2, 7, 5, 8, 4, 10, 5, 4, 8, 2, 6, 1, 0, 11, 10, 2, 1, 10, 6, 0, 0, 11, 11, 6, 1, 9, 3, 1, 7, 9, 2, 11, 11, 1, 0, 10, 7, 1, 7, 10, 1, 4, 0, 0, 8, 7, 1, 2, 9, 7, 4, 6, 2, 6, 8, 1, 9, 6, 6, 7, 5, 0, 0, 3, 9, 8, 3, 6, 6, 11, 1, 0, 0, 7, 4, 5, 7, 6, 3, 11, 1, 9, 11, 0, 5, 2, 5, 7, 9, 8, 0, 7, 6, 9, 10, 8, 3, 1, 0, 9, 10, 11, 10, 6, 4, 7, 0, 6, 3, 0, 2, 5, 2, 10, 0, 3, 11, 9, 11, 11, 8, 9, 9, 9, 4, 9, 5, 8, 3, 6, 8, 5, 4, 3, 0, 8, 7, 2, 9, 11, 2, 7, 0, 3, 10, 5, 2, 2, 3, 11, 3, 1, 2, 0, 7, 1, 2, 4, 9, 8, 5, 7, 10, 5, 4, 4, 6, 11, 6, 5, 1, 3, 5, 1, 0, 8, 1, 5, 4, 0, 7, 4, 5, 6, 1, 8, 4, 3, 10, 8, 8, 3, 2, 8, 4, 1, 6, 5, 6, 3, 4, 4, 1, 10, 10, 4, 3, 5, 10, 2, 3, 10, 6, 3, 10, 1, 8, 3, 2, 11, 11, 11, 4, 10, 5, 2, 9, 4, 6, 7, 3, 2, 9, 11, 8, 8, 2, 8, 10, 7, 10, 5, 9, 5, 11, 11, 7, 4, 9, 9, 10, 3, 1, 7, 2, 0, 2, 7, 5, 8, 4, 10, 5, 4, 8, 2, 6, 1, 0, 11, 10, 2, 1, 10, 6, 0, 0, 11, 11, 6, 1, 9, 3, 1, 7, 9, 2, 11, 11, 1, 0, 10, 7, 1, 7, 10, 1, 4, 0, 0, 8, 7, 1, 2, 9, 7, 4, 6, 2, 6, 8, 1, 9, 6, 6, 7, 5, 0, 0, 3, 9, 8, 3, 6, 6, 11, 1, 0, 0
);
const float F2 = 0.36602542;
const float G2 = 0.21132487;
const float F3 = 0.33333334;
const float G3 = 0.16666667;
const float F4 = 0.309017;
const float G4 = 0.1381966;

float dot(uvec3 g, vec2 v) {
    return dot(vec3(g).xy, v);
}

float dot(uvec3 g, vec3 v) {
    return dot(vec3(g), v);
}

float simplex_noise(vec2 xy) {
    float x = xy.x, y = xy.y;
//    float s = (x + y) * 0.36602542F;
    float s = (x + y) * F2;
//    int i = fastfloor(x + s);
//    int j = fastfloor(y + s);
    vec2 ij_f = floor(xy + s);
    ivec2 ij_i = ivec2(ij_f);
//    float t = (float)(i + j) * 0.21132487F;
    float t = (ij_f.x + ij_f.y) * G2;
//    float X0 = (float)i - t;
//    float Y0 = (float)j - t;
    vec2 XY0 = ij_f - t;
//    float x0 = x - X0;
//    float y0 = y - Y0;
    vec2 xy0 = xy - XY0;
//    int i1;
//    int j1;
//    if (x0 > y0) {
//        i1 = 1;
//        j1 = 0;
//    } else {
//        i1 = 0;
//        j1 = 1;
//    }
    uvec2 ij1_i = xy0.x > xy0.y ? ivec2(1, 0) : ivec2(0, 1);
    vec2 ij1_f = ij1_i;
//    float x1 = x0 - (float)i1 + 0.21132487F;
//    float y1 = y0 - (float)j1 + 0.21132487F;
    vec2 xy1 = xy0 - ij1_f + G2;
//    float x2 = x0 - 1.0F + 0.42264974F;
//    float y2 = y0 - 1.0F + 0.42264974F;
    vec2 xy2 = xy0 - (F2 + G2);
//    int ii = i & 255;
//    int jj = j & 255;
    uvec2 iijj = ij_i & 255;
    uint ii = iijj.x;
    uint jj = iijj.y;
//    int gi0 = permMod12[ii + perm[jj] & 255] & 255;
//    int gi1 = permMod12[ii + i1 + perm[jj + j1] & 255] & 255;
//    int gi2 = permMod12[ii + 1 + perm[jj + 1] & 255] & 255;
    uint gi0 = permMod12[ii.x + perm[jj]];
    uint gi1 = permMod12[ii.x + ij1_i.x + perm[jj + ij1_i.y]];
    uint gi2 = permMod12[ii.x + 1 + perm[jj + 1]];
//    float t0 = 0.5 - x0 * x0 - y0 * y0;
    float t0 = 0.5 - dot(xy0, xy0);
//    float n0;
//    if (t0 < 0.0) {
//        n0 = 0.0;
//    } else {
//        t0 *= t0;
//        n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
//    }
    float n0;
    if (t0 < 0.0) {
        n0 = 0.0;
    } else {
        t0 *= t0;
        n0 = t0 * t0 * dot(grad3[gi0], xy0);
    }
//    float t1 = 0.5 - x1 * x1 - y1 * y1;
    float t1 = 0.5 - dot(xy1, xy1);
//    float n1;
//    if (t1 < 0.0) {
//        n1 = 0.0;
//    } else {
//        t1 *= t1;
//        n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
//    }
    float n1;
    if (t1 < 0.0) {
        n1 = 0.0;
    } else {
        t1 *= t1;
        n1 = t1 * t1 * dot(grad3[gi1], xy1);
    }
//    float t2 = 0.5 - x2 * x2 - y2 * y2;
    float t2 = 0.5 - dot(xy2, xy2);
//    float n2;
//    if (t2 < 0.0) {
//        n2 = 0.0;
//    } else {
//        t2 *= t2;
//        n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
//    }
    float n2;
    if (t2 < 0.0) {
        n2 = 0.0;
    } else {
        t2 *= t2;
        n2 = t2 * t2 * dot(grad3[gi2], xy2);
    }

//    return 70.0 * (n0 + n1 + n2);
    return 70.0 * (n0 + n1 + n2);
}

//float noise(float x, float y, float z) {
//    float s = (x + y + z) * 0.33333334;
//    int i = fastfloor(x + s);
//    int j = fastfloor(y + s);
//    int k = fastfloor(z + s);
//    float t = (float)(i + j + k) * 0.16666667;
//    float X0 = (float)i - t;
//    float Y0 = (float)j - t;
//    float Z0 = (float)k - t;
//    float x0 = x - X0;
//    float y0 = y - Y0;
//    float z0 = z - Z0;
//    int i1;
//    int j1;
//    int k1;
//    int i2;
//    int j2;
//    int k2;
//    if (x0 >= y0) {
//        if (y0 >= z0) {
//            i1 = 1;
//            j1 = 0;
//            k1 = 0;
//            i2 = 1;
//            j2 = 1;
//            k2 = 0;
//        } else if (x0 >= z0) {
//            i1 = 1;
//            j1 = 0;
//            k1 = 0;
//            i2 = 1;
//            j2 = 0;
//            k2 = 1;
//        } else {
//            i1 = 0;
//            j1 = 0;
//            k1 = 1;
//            i2 = 1;
//            j2 = 0;
//            k2 = 1;
//        }
//    } else if (y0 < z0) {
//        i1 = 0;
//        j1 = 0;
//        k1 = 1;
//        i2 = 0;
//        j2 = 1;
//        k2 = 1;
//    } else if (x0 < z0) {
//        i1 = 0;
//        j1 = 1;
//        k1 = 0;
//        i2 = 0;
//        j2 = 1;
//        k2 = 1;
//    } else {
//        i1 = 0;
//        j1 = 1;
//        k1 = 0;
//        i2 = 1;
//        j2 = 1;
//        k2 = 0;
//    }
//
//    float x1 = x0 - (float)i1 + 0.16666667;
//    float y1 = y0 - (float)j1 + 0.16666667;
//    float z1 = z0 - (float)k1 + 0.16666667;
//    float x2 = x0 - (float)i2 + 0.33333334;
//    float y2 = y0 - (float)j2 + 0.33333334;
//    float z2 = z0 - (float)k2 + 0.33333334;
//    float x3 = x0 - 1.0 + 0.5;
//    float y3 = y0 - 1.0 + 0.5;
//    float z3 = z0 - 1.0 + 0.5;
//    int ii = i & 255;
//    int jj = j & 255;
//    int kk = k & 255;
//    int gi0 = permMod12[ii + perm[jj + perm[kk] & 255] & 255] & 255;
//    int gi1 = permMod12[ii + i1 + perm[jj + j1 + perm[kk + k1] & 255] & 255] & 255;
//    int gi2 = permMod12[ii + i2 + perm[jj + j2 + perm[kk + k2] & 255] & 255] & 255;
//    int gi3 = permMod12[ii + 1 + perm[jj + 1 + perm[kk + 1] & 255] & 255] & 255;
//    float t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
//    float n0;
//    if (t0 < 0.0) {
//        n0 = 0.0;
//    } else {
//        t0 *= t0;
//        n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
//    }
//
//    float t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
//    float n1;
//    if (t1 < 0.0) {
//        n1 = 0.0;
//    } else {
//        t1 *= t1;
//        n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
//    }
//
//    float t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
//    float n2;
//    if (t2 < 0.0) {
//        n2 = 0.0;
//    } else {
//        t2 *= t2;
//        n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
//    }
//
//    float t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
//    float n3;
//    if (t3 < 0.0) {
//        n3 = 0.0;
//    } else {
//        t3 *= t3;
//        n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
//    }
//
//    return 32.0 * (n0 + n1 + n2 + n3);
//}
//
//float noise(float x, float y, float z, float w) {
//    float s = (x + y + z + w) * 0.309017;
//    int i = fastfloor(x + s);
//    int j = fastfloor(y + s);
//    int k = fastfloor(z + s);
//    int l = fastfloor(w + s);
//    float t = (float)(i + j + k + l) * 0.1381966;
//    float X0 = (float)i - t;
//    float Y0 = (float)j - t;
//    float Z0 = (float)k - t;
//    float W0 = (float)l - t;
//    float x0 = x - X0;
//    float y0 = y - Y0;
//    float z0 = z - Z0;
//    float w0 = w - W0;
//    int rankx = 0;
//    int ranky = 0;
//    int rankz = 0;
//    int rankw = 0;
//    if (x0 > y0) {
//        ++rankx;
//    } else {
//        ++ranky;
//    }
//
//    if (x0 > z0) {
//        ++rankx;
//    } else {
//        ++rankz;
//    }
//
//    if (x0 > w0) {
//        ++rankx;
//    } else {
//        ++rankw;
//    }
//
//    if (y0 > z0) {
//        ++ranky;
//    } else {
//        ++rankz;
//    }
//
//    if (y0 > w0) {
//        ++ranky;
//    } else {
//        ++rankw;
//    }
//
//    if (z0 > w0) {
//        ++rankz;
//    } else {
//        ++rankw;
//    }
//
//    int i1 = rankx >= 3 ? 1 : 0;
//    int j1 = ranky >= 3 ? 1 : 0;
//    int k1 = rankz >= 3 ? 1 : 0;
//    int l1 = rankw >= 3 ? 1 : 0;
//    int i2 = rankx >= 2 ? 1 : 0;
//    int j2 = ranky >= 2 ? 1 : 0;
//    int k2 = rankz >= 2 ? 1 : 0;
//    int l2 = rankw >= 2 ? 1 : 0;
//    int i3 = rankx >= 1 ? 1 : 0;
//    int j3 = ranky >= 1 ? 1 : 0;
//    int k3 = rankz >= 1 ? 1 : 0;
//    int l3 = rankw >= 1 ? 1 : 0;
//    float x1 = x0 - (float)i1 + 0.1381966;
//    float y1 = y0 - (float)j1 + 0.1381966;
//    float z1 = z0 - (float)k1 + 0.1381966;
//    float w1 = w0 - (float)l1 + 0.1381966;
//    float x2 = x0 - (float)i2 + 0.2763932;
//    float y2 = y0 - (float)j2 + 0.2763932;
//    float z2 = z0 - (float)k2 + 0.2763932;
//    float w2 = w0 - (float)l2 + 0.2763932;
//    float x3 = x0 - (float)i3 + 0.41458982;
//    float y3 = y0 - (float)j3 + 0.41458982;
//    float z3 = z0 - (float)k3 + 0.41458982;
//    float w3 = w0 - (float)l3 + 0.41458982;
//    float x4 = x0 - 1.0 + 0.5527864;
//    float y4 = y0 - 1.0 + 0.5527864;
//    float z4 = z0 - 1.0 + 0.5527864;
//    float w4 = w0 - 1.0 + 0.5527864;
//    int ii = i & 255;
//    int jj = j & 255;
//    int kk = k & 255;
//    int ll = l & 255;
//    int gi0 = (perm[ii + perm[jj + perm[kk + perm[ll] & 255] & 255] & 255] & 255) % 32;
//    int gi1 = (perm[ii + i1 + perm[jj + j1 + perm[kk + k1 + perm[ll + l1] & 255] & 255] & 255] & 255) % 32;
//    int gi2 = (perm[ii + i2 + perm[jj + j2 + perm[kk + k2 + perm[ll + l2] & 255] & 255] & 255] & 255) % 32;
//    int gi3 = (perm[ii + i3 + perm[jj + j3 + perm[kk + k3 + perm[ll + l3] & 255] & 255] & 255] & 255) % 32;
//    int gi4 = (perm[ii + 1 + perm[jj + 1 + perm[kk + 1 + perm[ll + 1] & 255] & 255] & 255] & 255) % 32;
//    float t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
//    float n0;
//    if (t0 < 0.0) {
//        n0 = 0.0;
//    } else {
//        t0 *= t0;
//        n0 = t0 * t0 * dot(grad4[gi0], x0, y0, z0, w0);
//    }
//
//    float t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
//    float n1;
//    if (t1 < 0.0) {
//        n1 = 0.0;
//    } else {
//        t1 *= t1;
//        n1 = t1 * t1 * dot(grad4[gi1], x1, y1, z1, w1);
//    }
//
//    float t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
//    float n2;
//    if (t2 < 0.0) {
//        n2 = 0.0;
//    } else {
//        t2 *= t2;
//        n2 = t2 * t2 * dot(grad4[gi2], x2, y2, z2, w2);
//    }
//
//    float t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
//    float n3;
//    if (t3 < 0.0) {
//        n3 = 0.0;
//    } else {
//        t3 *= t3;
//        n3 = t3 * t3 * dot(grad4[gi3], x3, y3, z3, w3);
//    }
//
//    float t4 = 0.6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
//    float n4;
//    if (t4 < 0.0) {
//        n4 = 0.0;
//    } else {
//        t4 *= t4;
//        n4 = t4 * t4 * dot(grad4[gi4], x4, y4, z4, w4);
//    }
//
//    return 27.0 * (n0 + n1 + n2 + n3 + n4);
//}
