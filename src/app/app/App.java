package app.app;

import app.atlas.TextureAtlas;
import app.world.CubeRaycaster;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.worldgen.WorldGenerator;
import j3d.IAppLogic;
import j3d.MouseInput;
import j3d.Window;
import j3d.graph.Mesh;
import j3d.graph.ShaderProgram;
import j3d.graph.Texture;
import j3d.graph.TextureCache;
import j3d.scene.Camera;
import j3d.scene.Projection;
import org.joml.*;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class App implements IAppLogic {
    private static final float MOUSE_SENSITIVITY = 0.5f;
    private static final float MOVEMENT_SPEED = 0.015f;
    private Camera camera;
    private Projection projection;

    private Mesh blockOutlineMesh;

    private ShaderProgram worldShader;
    private ShaderProgram outlineShader;
    private ShaderProgram uiShader;
    private World world;
    private TextureCache textureCache;
    private Window window;

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window) {
        this.window = window;
        projection = new Projection(this.window.getWidth(), window.getHeight());

        blockOutlineMesh = new Mesh(
            new int[]  {
                0, 1, 2, 1, 2, 3,
                4, 5, 6, 5, 6, 7,
                0, 2, 4, 2, 4, 6,
                1, 3, 5, 3, 5, 6,
                0, 1, 4, 1, 4, 5,
                2, 3, 6, 3, 6, 7
            },
            Mesh.MeshAttributeData.create(
                3,
                new float[]{
                        -.501f, -.501f, -.501f,
                        .501f, -.501f, -.501f,
                        -.501f,  .501f, -.501f,
                        .501f,  .501f, -.501f,
                        -.501f, -.501f,  .501f,
                        .501f, -.501f,  .501f,
                        -.501f,  .501f,  .501f,
                        .501f,  .501f,  .501f,
                }
            )
        );

        worldShader = ShaderProgram.createBasicShaderProgram(
                "shaders/chunk/chunk.vert",
                "shaders/chunk/chunk.frag"
        );
        outlineShader = ShaderProgram.createBasicShaderProgram(
                "shaders/block-outline/outline.vert",
                "shaders/block-outline/outline.frag"
        );
        uiShader = ShaderProgram.createBasicShaderProgram(
            "shaders/ui/ui.vert",
            "shaders/ui/ui.frag"
        );

        textureCache = new TextureCache();

        camera = new Camera();

        glClearColor(.7f, .93f, 1f, 1f);

        TextureAtlas.setTexture(textureCache.createTexture("atlas.png"));
        textureCache.createTexture("crosshair.png");

        DefaultBlocksInitializer.run();

        world = new World(new WorldGenerator());

        System.out.println("Generating world...");
        int loadRange = 1;
        for(int x = -loadRange; x <= loadRange; x++) {
            for(int z = -loadRange; z <= loadRange; z++) {
                world.loadChunkAtPosition(x, z);
            }
        }
        System.out.println("Generated " + world.getVisibleChunks().size() + " chunks");
        world.recalculateLightingForAllVisibleChunks();
        System.out.println("Preloading meshes");
        world.getVisibleChunks().forEach(Chunk::getMesh);
        System.out.println("Running...");
    }

    @Override
    public void input(Window window, long deltaTime) {
        float move = deltaTime * MOVEMENT_SPEED;
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.move(new Vector3f(0, 0, -1).rotateY(-camera.getRotation().y).mul(move));
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.move(new Vector3f(0, 0, 1).rotateY(-camera.getRotation().y).mul(move));
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.move(new Vector3f(-1, 0, 0).rotateY(-camera.getRotation().y).mul(move));
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.move(new Vector3f(1, 0, 0).rotateY(-camera.getRotation().y).mul(move));
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.move(new Vector3f(0, 1, 0).mul(move));
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.move(new Vector3f(0, -1, 0).mul(move));
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f movement = mouseInput.getMovement();
            camera.rotateBy(
                    (float) Math.toRadians(movement.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(movement.y * MOUSE_SENSITIVITY));
        }
    }

    @Override
    public void update(Window window, long deltaTime) {
        // TODO
    }

    @Override
    public void draw(Window window) {
        startRender(window);

        drawChunks();
        drawSelectedPosition();
        drawUI();
    }

    private void drawUI() {
        drawCrossHair();
    }

    private Mesh crossHairMesh;
    private void drawCrossHair() {
        if(crossHairMesh == null) crossHairMesh = new Mesh(
            new int[]{0, 1, 2, 0, 2, 3},
            Mesh.MeshAttributeData.create(2, new float[] {
                -1, -1, -1, 1, 1, 1, 1, -1
            }),
            Mesh.MeshAttributeData.create(2, new float[] {
                0, 0, 0, 1, 1, 1, 1, 0
            })
        );

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDisable(GL_CULL_FACE);

        uiShader.bind();

        float aspect = 1f * window.getHeight() / window.getWidth();

        uiShader.uniforms().setUniform("projectionMatrix", new Matrix4f(
            aspect, 0, 0, 0,
            0, -1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        ).scale(.02f, .02f, 0));
        uiShader.uniforms().setUniform("txtSampler", 0);

        glActiveTexture(GL_TEXTURE0);
        textureCache.getTexture("crosshair.png").bind();

        // Render stuff

        glBindVertexArray(crossHairMesh.getVaoId());
        glDrawElements(GL_TRIANGLES, crossHairMesh.getNumIndices(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        outlineShader.unbind();
    }

    private void drawSelectedPosition() {

        Vector3f pos = null;
        {
            var cast = new CubeRaycaster().cast(camera);
            for(int i = 0; i < 50; i++) {
                var castPos = cast.next();
                pos = new Vector3f(castPos);
                if(!world.isBlockLoaded(castPos.x, castPos.y, castPos.z)) {
                    pos = null;
                    break;
                }
                if(world.getBlockIDAt(castPos.x, castPos.y, castPos.z) != 0) {
                    break;
                }
            }
        }

        if(pos != null) {

            glLineWidth(2);
            glEnable(GL_LINE_SMOOTH);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);

            outlineShader.bind();

            outlineShader.uniforms().setUniform("projectionMatrix", projection.getProjectionMatrix());
            outlineShader.uniforms().setUniform("viewMatrix", camera.getViewMatrix());

            outlineShader.uniforms().setUniform("txtSampler", 0);

            // Render stuff

            Matrix4f modelMatrix = new Matrix4f().translationRotateScale(pos, new Quaternionf(), 1);
            worldShader.uniforms().setUniform("modelMatrix", modelMatrix);
            glBindVertexArray(blockOutlineMesh.getVaoId());
            glDrawElements(GL_TRIANGLES, blockOutlineMesh.getNumIndices(), GL_UNSIGNED_INT, 0);

            glBindVertexArray(0);
            outlineShader.unbind();
        }
    }

    private void drawChunks() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_CULL_FACE);

        worldShader.bind();

        worldShader.uniforms().setUniform("projectionMatrix", projection.getProjectionMatrix());
        worldShader.uniforms().setUniform("viewMatrix", camera.getViewMatrix());

        worldShader.uniforms().setUniform("txtSampler", 0);

        // Render stuff

        glActiveTexture(GL_TEXTURE0);
        TextureAtlas.get().bind();

        for(Chunk chunk : world.getVisibleChunks()) {
            Vector2i chunkPosition = chunk.getChunkPosition();

            Matrix4f modelMatrix = new Matrix4f().translationRotateScale(
                new Vector3f(chunkPosition.x, 0, chunkPosition.y),
                new Quaternionf(), 1
            );

            worldShader.uniforms().setUniform("modelMatrix", modelMatrix);
            glBindVertexArray(chunk.getMesh().getVaoId());
            glDrawElements(GL_TRIANGLES, chunk.getMesh().getNumIndices(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
        worldShader.unbind();
    }

    private static void startRender(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        projection.updateProjectionMatrix(width, height);
    }
}