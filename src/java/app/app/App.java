package app.app;

import app.atlas.TextureAtlas;
import app.block.BlockRegistry;
import app.player.Player;
import app.world.CubeRaycaster;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.util.IVec3i;
import app.world.util.WorldPosition;
import app.world.worldgen.WorldGenerator;
import j3d.IAppLogic;
import j3d.MouseInput;
import j3d.Window;
import j3d.graph.Mesh;
import j3d.graph.Shader;
import j3d.graph.Texture;
import j3d.scene.Projection;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;
import static util.MiscUtil.boolToInt;

public class App implements IAppLogic {
    private static final float MOUSE_SENSITIVITY = 0.5f;
    private static final float MOVEMENT_SPEED = 0.015f;

    private Player player;
    private Projection projection;
    private Mesh blockOutlineMesh;

    private Shader worldShader;
    private Shader outlineShader;
    private Shader uiShader;
    private World world;
    private Window window;
    private Mesh crossHairMesh;
    private static final int renderDistance = 100;
    private static final int loadDistance = 80;

    private static void startRender(Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());
    }

    @Override
    public void freeResources() {
        world.freeResources();
    }

    @Override
    public void init(Window window) {
        this.window = window;
        projection = new Projection(this.window.getWidth(), window.getHeight());

        blockOutlineMesh = new Mesh(
            new int[]{
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
                    -.501f, .501f, -.501f,
                    .501f, .501f, -.501f,
                    -.501f, -.501f, .501f,
                    .501f, -.501f, .501f,
                    -.501f, .501f, .501f,
                    .501f, .501f, .501f,
                }
            )
        );

        worldShader = Shader.createBasicShader(
            "shaders/chunk/chunk.vert",
            "shaders/chunk/chunk.frag"
        );
        outlineShader = Shader.createBasicShader(
            "shaders/block-outline/outline.vert",
            "shaders/block-outline/outline.frag"
        );
        uiShader = Shader.createBasicShader(
            "shaders/ui/ui.vert",
            "shaders/ui/ui.frag"
        );

        player = new Player();

        glClearColor(.7f, .93f, 1f, 1f);

        TextureAtlas.setTexture(new Texture("atlas.png"));

        DefaultBlocksInitializer.run();

        world = new World(new WorldGenerator(), player);

        System.out.println("Running...");

        window.addMouseListener(this::onMouseDown);

        world.getOrLoadChunk(new ChunkOffset(0, 0), true);
        for (int y = 0; y < World.CHUNK_HEIGHT; y++) {
            player.setPosition(new Vector3f(0, y, 0));
            if (world.getBlockIDAt(new WorldPosition(0, y, 0)) == 0) break;
        }
    }

    private void updateLighting() {
        world.updateLighting();
    }

    @Override
    public void input(Window window, int deltaTime) {
        float move = deltaTime * MOVEMENT_SPEED;

        Vector3f playerMovement = new Vector3f(
            boolToInt(window.isKeyPressed(GLFW_KEY_D))
                - boolToInt(window.isKeyPressed(GLFW_KEY_A)),
            boolToInt(window.isKeyPressed(GLFW_KEY_SPACE))
                - boolToInt(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)),
            boolToInt(window.isKeyPressed(GLFW_KEY_S))
                - boolToInt(window.isKeyPressed(GLFW_KEY_W))
        );

        player.move(playerMovement.rotateY(-player.getRotation().y).mul(move));

        MouseInput mouseInput = window.getMouseInput();
        Vector2f mouseMovement = mouseInput.getMovement();
        player.rotateBy(
            (float) Math.toRadians(mouseMovement.x * MOUSE_SENSITIVITY),
            (float) Math.toRadians(mouseMovement.y * MOUSE_SENSITIVITY)
        );

        if (player.getRotation().x >= Math.PI / 2) {
            player.setRotation((float) Math.PI / 2f, player.getRotation().y);
        }
        if (player.getRotation().x <= -Math.PI / 2) {
            player.setRotation((float) -Math.PI / 2f, player.getRotation().y);
        }
    }

    @Override
    public void update(Window window, int deltaTime) {
        player.update(deltaTime);

        if (window.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            deleteBlock();
        } else if (window.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            placeBlock();
        }

        WorldPosition playerPos = IVec3i.fromVector3f(player.getPosition(), new WorldPosition());

        // Load chunks around the player
        for (int dx = -loadDistance; dx <= loadDistance; dx++) {
            for (int dz = -loadDistance; dz <= loadDistance; dz++) {
                if (dx * dx + dz * dz > loadDistance * loadDistance) continue;
                world.getOrLoadChunkAtPos(
                    new WorldPosition(playerPos.x() + dx, 0, playerPos.z() + dz),
                    false
                );
            }
        }
    }

    @Override
    public void draw(Window window) {
        updateLighting();

        startRender(window);

        drawChunks();
        drawSelectedPosition();
        drawUI();
    }

    private void drawUI() {
        drawCrossHair();
    }

    private void drawCrossHair() {
        if (crossHairMesh == null) crossHairMesh = new Mesh(
            new int[]{0, 1, 2, 0, 2, 3},
            Mesh.MeshAttributeData.create(2, new float[]{
                -1, -1, -1, 1, 1, 1, 1, -1
            }),
            Mesh.MeshAttributeData.create(2, new float[]{
                0, 0, 0, 1, 1, 1, 1, 0
            })
        );

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glDisable(GL_CULL_FACE);
        glEnable(GL_COLOR_LOGIC_OP);
        glLogicOp(GL_XOR);

        uiShader.bind();

        float aspect = 1f * window.getHeight() / window.getWidth();

        uiShader.uniforms().setUniform("projectionMatrix", new Matrix4f(
            aspect, 0, 0, 0,
            0, -1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        ).scale(.02f, .02f, 0));
        uiShader.uniforms().setUniform("txtSampler", 0);

        Texture.getCached("crosshair.png").bind();

        // Render stuff

        glBindVertexArray(crossHairMesh.getVaoID());
        glDrawElements(GL_TRIANGLES, crossHairMesh.getNumIndices(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        outlineShader.unbind();

        glDisable(GL_COLOR_LOGIC_OP);
    }

    private void drawSelectedPosition() {

        Vector3f pos = null;
        {
            var cast = new CubeRaycaster().cast(player.getCamera());
            for (int i = 0; i < 100; i++) {
                var castPos = cast.next();
                pos = castPos.toVector3f();
                if (!world.isBlockLoaded(castPos) || player.getPosition().distance(pos) > 10) {
                    pos = null;
                    break;
                }
                if (world.getBlockIDAt(castPos) != 0) {
                    break;
                }
            }
        }

        if (pos != null) {

            glLineWidth(2);
            glEnable(GL_LINE_SMOOTH);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_CULL_FACE);

            outlineShader.bind();

            outlineShader.uniforms().setUniform("projectionMatrix", projection.getProjectionMatrix());
            outlineShader.uniforms().setUniform("viewMatrix", player.getCamera().getViewMatrix());

            outlineShader.uniforms().setUniform("txtSampler", 0);

            // Render stuff

            Matrix4f modelMatrix = new Matrix4f().translationRotateScale(pos, new Quaternionf(), 1);
            worldShader.uniforms().setUniform("modelMatrix", modelMatrix);
            glBindVertexArray(blockOutlineMesh.getVaoID());
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
        worldShader.uniforms().setUniform("viewMatrix", player.getCamera().getViewMatrix());

        worldShader.uniforms().setUniform("txtSampler", 0);

        // Render stuff
        TextureAtlas.get().bind();

        WorldPosition playerPos = IVec3i.fromVector3f(player.getPosition(), new WorldPosition());
        HashMap<ChunkOffset, Chunk> chunksToRender = new HashMap<>();
        for (int dx = -renderDistance; dx <= renderDistance; dx++) {
            for (int dz = -renderDistance; dz <= renderDistance; dz++) {
                if (dx * dx + dz * dz > renderDistance * renderDistance) continue;
                chunksToRender.computeIfAbsent(
                    new WorldPosition(playerPos.x() + dx, 0, playerPos.z() + dz).getChunkOffset(),
                    offset -> world.getOrLoadChunk(offset, false)
                );
            }
        }

        for (Map.Entry<ChunkOffset, Chunk> entry : chunksToRender.entrySet()) {
            ChunkOffset chunkOffset = entry.getKey();
            Chunk chunk = entry.getValue();
            Matrix4f modelMatrix = new Matrix4f().translationRotateScale(
                new Vector3f(chunkOffset.x(), 0, chunkOffset.z()),
                new Quaternionf(), 1
            );

            worldShader.uniforms().setUniform("modelMatrix", modelMatrix);
            glBindVertexArray(chunk.getMesh().getVaoID());
            glDrawElements(GL_TRIANGLES, chunk.getMesh().getNumIndices(), GL_UNSIGNED_INT, 0);
        }

        glBindVertexArray(0);
        worldShader.unbind();
    }

    @Override
    public void resize(int width, int height) {
        projection.updateProjectionMatrix(width, height);
    }

    private void onMouseDown(int button, int action) {
        if (action == GLFW_PRESS) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (player.deleteBlockCooldown.isFinished()) {
                    deleteBlock();
                    player.deleteBlockCooldown.reset(300);
                }
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                if (player.placeBlockCooldown.isFinished()) {
                    placeBlock();
                    player.placeBlockCooldown.reset(300);
                }
            }
        }
    }

    private void placeBlock() {
        if (!player.placeBlockCooldown.isFinished()) return;
        player.placeBlockCooldown.reset();

        WorldPosition pos = null;
        {
            var cast = new CubeRaycaster().cast(player.getCamera());
            for (int i = 0; i < 100; i++) {
                var n = cast.next();
                if (!world.isBlockLoaded(n) ||
                    player.getPosition().distance(n.toVector3f()) > 10
                ) {
                    pos = null;
                    break;
                }
                if (world.getBlockIDAt(n) != 0) {
                    break;
                }
                pos = n;
            }
        }

        if (pos != null) {
            world.setBlockIDAt(pos, BlockRegistry.getBlockID("iron_ore"));
        }
    }

    private void deleteBlock() {
        if (!player.deleteBlockCooldown.isFinished()) return;
        player.deleteBlockCooldown.reset();

        WorldPosition pos = null;
        {
            var cast = new CubeRaycaster().cast(player.getCamera());
            for (int i = 0; i < 100; i++) {
                pos = cast.next();
                if (!world.isBlockLoaded(pos) ||
                    player.getPosition().distance(pos.toVector3f()) > 10
                ) {
                    pos = null;
                    break;
                }
                if (world.getBlockIDAt(pos) != 0) {
                    break;
                }
            }
        }

        if (pos != null) {
            world.setBlockIDAt(pos, 0);
        }
    }
}