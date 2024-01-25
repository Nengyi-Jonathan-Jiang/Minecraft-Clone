package app.app;

import app.atlas.TextureAtlas;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.worldgen.WorldGenerator;
import j3d.IAppLogic;
import j3d.MouseInput;
import j3d.Window;
import j3d.graph.ShaderProgram;
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
    private TextureCache textureCache;


    private ShaderProgram worldShader;
    private World world;

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window) {
        projection = new Projection(window.getWidth(), window.getHeight());

        worldShader = ShaderProgram.createBasicShaderProgram(
                "shaders/scene.vert",
                "shaders/scene.frag"
        );
        textureCache = new TextureCache();

        camera = new Camera();

        glClearColor(.7f, .93f, 1f, 1f);

        TextureAtlas.setTexture(textureCache.createTexture("atlas.png"));

        DefaultBlocksInitializer.run();

        world = new World(new WorldGenerator());

        int loadRange = 64;
        for(int x = -loadRange; x <= loadRange; x++) for(int z = -loadRange; z <= loadRange; z++) world.loadChunkAtPosition(x, z);
        world.recalculateLightingForAllVisibleChunks();
    }

    @Override
    public void input(Window window, long deltaTime) {
        float move = deltaTime * MOVEMENT_SPEED;
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveDown(move);
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

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
                new Vector3f(chunkPosition.x * Chunk.SIZE, 0, chunkPosition.y * Chunk.SIZE),
                new Quaternionf(), 1
            );

            worldShader.uniforms().setUniform("modelMatrix", modelMatrix);
            glBindVertexArray(chunk.getMesh().getVaoId());
            glDrawElements(GL_TRIANGLES, chunk.getMesh().getNumVertices(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
        worldShader.unbind();
    }

    @Override
    public void resize(int width, int height) {
        projection.updateProjectionMatrix(width, height);
    }
}