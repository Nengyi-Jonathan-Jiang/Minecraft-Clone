layout (binding = 1) uniform permArrays {
    ivec3 grad3[12];
    uint perm[512];
    uint permMod12[512];
};
const float F2 = 0.36602542;
const float G2 = 0.21132487;
const float F3 = 0.33333334;
const float G3 = 0.16666667;
const float F4 = 0.309017;
const float G4 = 0.1381966;

float _dot(ivec3 g, vec2 v) {
    return dot(vec3(g).xy, v);
}

float _dot(ivec3 g, vec3 v) {
    return dot(vec3(g), v);
}

float simplex_noise(vec2 xy) {
    // Transform xy to lie on a squashed hypercubic honeycomb lattice, such that
    //   dist(T(0, 0), T(1, 1)) = dist(T(0, 0), T(0, 1)) = dist(T(0, 0), T(1, 0))
    vec2 xy_transformed = xy + dot(xy, vec2(F2));

    // Figure out which hypercube the transformed point is in.
    vec2 ij = floor(xy_transformed);

    // Magic to figure out the three vertices of the containing simplex
    vec2 XY0 = ij - dot(ij, vec2(G2));
    vec2 xy0 = xy - XY0;
    uvec2 ij1 = xy0.x > xy0.y ? ivec2(1, 0) : ivec2(0, 1);
    vec2 xy1 = xy0 - ij1 + G2;
    vec2 xy2 = xy0 - (F2 + G2);

    uvec2 iijj = ivec2(ij) & 0xff;
    uint ii = iijj.x;
    uint jj = iijj.y;

    uint gi0 = permMod12[ii + perm[jj]];
    uint gi1 = permMod12[ii + ij1.x + perm[jj + ij1.y]];
    uint gi2 = permMod12[ii + 1 + perm[jj + 1]];

    float t0 = max(0.0, 0.5 - dot(xy0, xy0));
    float n0 = (t0 * t0) * (t0 * t0) * _dot(grad3[gi0], xy0);

    float t1 = max(0.0, 0.5 - dot(xy1, xy1));
    float n1 = (t1 * t1) * (t1 * t1) * _dot(grad3[gi1], xy1);

    float t2 = max(0.0, 0.5 - dot(xy2, xy2));
    float n2 = (t2 * t2) * (t2 * t2) * _dot(grad3[gi2], xy2);

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