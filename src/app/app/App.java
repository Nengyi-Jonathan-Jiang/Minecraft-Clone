package app.app;

import app.atlas.TextureAtlas;
import app.block.BlockRegistry;
import app.chunk.Chunk;
import j3d.IAppLogic;
import j3d.MouseInput;
import j3d.Window;
import j3d.graph.*;
import j3d.scene.Camera;
import j3d.scene.Entity;
import j3d.scene.Scene;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;

public class App implements IAppLogic {
    private static final float MOUSE_SENSITIVITY = 0.5f;
    private static final float MOVEMENT_SPEED = 0.015f;
    private Entity cubeEntity;
    private float rotation;

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        TextureAtlas.setTexture(scene.getTextureCache().createTexture("atlas.png"));

        DefaultBlocksInitializer.run();

        Chunk chunk = new Chunk();
        for(int x = 0; x < Chunk.SIZE; x++) {
            for(int z = 0; z < Chunk.SIZE; z++) {
                int h = (x - 8) * (z - 8) / 10 + 4;

                for(int y = 1; y <= h && y < Chunk.HEIGHT; y++) {
                    chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("dirt"));

                    if(y == h) chunk.setBlockAt(x, y, z, BlockRegistry.getBlockID("grass"));
                }

                chunk.setBlockAt(x, 0, z, BlockRegistry.getBlockID("bedrock"));
            }
        }

        Mesh mesh = chunk.getMesh();

        Material material = new Material();
        material.setTexturePath(TextureAtlas.get().getTexturePath());
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        material.getMeshList().add(mesh);
        Model cubeModel = new Model("cube-model", materialList);
        scene.addModel(cubeModel);

        cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(-.5f, -.5f, -4f);
        scene.addEntity(cubeEntity);
    }

    @Override
    public void input(Window window, Scene scene, long deltaTime) {
        float move = deltaTime * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
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
    public void update(Window window, Scene scene, long deltaTime) {
//        rotation += 0.7f;
//        if (rotation > 360) {
//            rotation = 0;
//        }
//        cubeEntity.setRotation(1, 1.5f, 1, (float) Math.toRadians(rotation));
        cubeEntity.updateModelMatrix();
    }
}