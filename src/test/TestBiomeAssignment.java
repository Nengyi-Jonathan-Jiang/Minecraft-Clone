import j3d.IAppLogic;
import j3d.Window;
import j3d.graph.Mesh;
import j3d.graph.Shader;
import j3d.opengl.OpenGLEngine;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class TestBiomeAssignment {

    public static void main(String[] args) {

        new OpenGLEngine("Biome Test", (window) -> new IAppLogic() {
            final Shader shader;
            final Mesh mesh;

            {
                shader = Shader.createBasicShader(
                    "shaders/misc/test_simplex_noise.vert",
                    "shaders/misc/test_biome_assignment.frag"
                );

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

                mesh.draw();
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
