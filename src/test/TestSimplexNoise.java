import j3d.IAppLogic;
import j3d.Window;
import j3d.graph.GPUBuffer;
import j3d.graph.Mesh;
import j3d.graph.Shader;
import j3d.opengl.OpenGLEngine;
import org.joml.Matrix4f;
import util.ArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.*;

public class TestSimplexNoise {

    public static void main(String[] args) {

        new OpenGLEngine("Simplex Noise Test", (window) -> new IAppLogic() {
            final GPUBuffer permArray;
            final Shader shader;
            final Mesh mesh;

            {
                shader = Shader.createBasicShader(
                    "shaders/misc/test_simplex_noise.vert",
                    "shaders/misc/test_simplex_noise.frag"
                );

                permArray = new GPUBuffer(GPUBuffer.BufferType.UniformBuffer, GPUBuffer.BufferUsage.StaticCopy);
                try (var access = permArray.bind()) {
//                    byte[] p = new byte[]{-105, -96, -119, 91, 90, 15, -125, 13, -55, 95, 96, 53, -62, -23, 7, -31, -116, 36, 103, 30, 69, -114, 8, 99, 37, -16, 21, 10, 23, -66, 6, -108, -9, 120, -22, 75, 0, 26, -59, 62, 94, -4, -37, -53, 117, 35, 11, 32, 57, -79, 33, 88, -19, -107, 56, 87, -82, 20, 125, -120, -85, -88, 68, -81, 74, -91, 71, -122, -117, 48, 27, -90, 77, -110, -98, -25, 83, 111, -27, 122, 60, -45, -123, -26, -36, 105, 92, 41, 55, 46, -11, 40, -12, 102, -113, 54, 65, 25, 63, -95, 1, -40, 80, 73, -47, 76, -124, -69, -48, 89, 18, -87, -56, -60, -121, -126, 116, -68, -97, 86, -92, 100, 109, -58, -83, -70, 3, 64, 52, -39, -30, -6, 124, 123, 5, -54, 38, -109, 118, 126, -1, 82, 85, -44, -49, -50, 59, -29, 47, 16, 58, 17, -74, -67, 28, 42, -33, -73, -86, -43, 119, -8, -104, 2, 44, -102, -93, 70, -35, -103, 101, -101, -89, 43, -84, 9, -127, 22, 39, -3, 19, 98, 108, 110, 79, 113, -32, -24, -78, -71, 112, 104, -38, -10, 97, -28, -5, 34, -14, -63, -18, -46, -112, 12, -65, -77, -94, -15, 81, 51, -111, -21, -7, 14, -17, 107, 49, -64, -42, 31, -75, -57, 106, -99, -72, 84, -52, -80, 115, 121, 50, 45, 127, 4, -106, -2, -118, -20, -51, 93, -34, 114, 67, 29, 24, 72, -13, -115, -128, -61, 78, 66, -41, 61, -100, -76};
                    List<Integer> a = new ArrayList<>(IntStream.range(0, 256).boxed().toList());
                    Collections.shuffle(a);
                    int[] p = ArrayUtil.unbox(a.toArray(Integer[]::new));
                    int[] perm = new int[512];
                    int[] permMod12 = new int[512];
                    for (int i = 0; i < 512; ++i) {
                        perm[i] = p[i & 255];
                        permMod12[i] = (((perm[i] & 255) % 10 + 1) + 12) % 12;
                    }

                    int[] grad3 = {
                        1, 1, 0,
                        -1, 1, 0,
                        1, -1, 0,
                        -1, -1, 0,
                        1, 0, 1,
                        -1, 0, 1,
                        1, 0, -1,
                        -1, 0, -1,
                        0, 1, 1,
                        0, -1, 1,
                        0, 1, -1,
                        0, -1, -1,
                    };

                    int[] data = new int[3 * 12 + 512 + 512];
                    System.arraycopy(grad3, 0, data, 0, grad3.length);
                    for (int i = 0; i < 512; i++) {
                        data[i + 3 * 12] = perm[i];
                        data[i + 3 * 12 + 512] = permMod12[i];
                    }

                    access.setData(data);
                }

                mesh = new Mesh(
                    new int[]{0, 1, 2, 0, 2, 3},
                    Mesh.MeshAttributeData.create(2, new float[]{
                        -1, -1, -1, 1, 1, 1, 1, -1
                    }),
                    Mesh.MeshAttributeData.create(2, new float[]{
                        0, 0, 0, 1, 1, 1, 1, 0
                    })
                );
            }

            @Override
            public void input(Window window, int deltaTime) {

            }

            @Override
            public void update(Window window, int deltaTime) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glViewport(0, 0, window.getWidth(), window.getHeight());

                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                glDisable(GL_CULL_FACE);

                shader.bind();

                float aspect = 1f * window.getHeight() / window.getWidth();

                shader.uniforms().setUniform("projectionMatrix", new Matrix4f(
                    aspect, 0, 0, 0,
                    0, -1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1
                ));

                try (var ignored = permArray.bindToShader(1)) {
                    mesh.draw();
                }
                shader.unbind();
            }

            @Override
            public void draw(Window window) {

            }

            @Override
            public void resize(int width, int height) {

            }

            @Override
            public void freeResources() {

            }
        }).run();
    }
}
