package app.world.worldgen;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import j3d.graph.GPUBuffer;
import j3d.graph.GPUBuffer.BufferAccess.ImmutableStorageAccessFlags;
import j3d.graph.Shader;
import j3d.opengl.OpenGLEngine;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL43.*;

// TODO: run on GPU
public class GPUWorldGenerator extends DefaultWorldGenerator {
    private final Shader shader;
    private final GPUBuffer buffer;

    public GPUWorldGenerator() {
        this.shader = new Shader(new Shader.ShaderModuleData(
            "shaders/compute/basic_terrain.glsl",
            Shader.ShaderType.Compute
        ));
        buffer = new GPUBuffer(
            GPUBuffer.BufferType.ShaderStorageBuffer,
            GPUBuffer.BufferUsage.StaticRead
        );
        try (var access = buffer.bind()) {
            access.allocateImmutably(
                World.BLOCKS_PER_CHUNK * 4,
                ImmutableStorageAccessFlags.Read
            );
//            access.setData(new int[World.BLOCKS_PER_CHUNK]);
        }
    }

    @Override
    public Chunk generateChunk(ChunkOffset chunkOffset, World world) {
        if (!OpenGLEngine.isGLInitialized()) {
            System.out.println("Initializing OpenGLContext");
            OpenGLEngine.initializeHeadless();
        }
        return super.generateChunk(chunkOffset, world);
    }

    @Override
    protected void addTerrainLayer(Chunk chunk) {
        shader.bind();
        try (var ignored = buffer.bindToShader(0)) {
            glDispatchCompute(World.CHUNK_SIZE, World.CHUNK_HEIGHT, World.CHUNK_SIZE);
//            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        }
        try (var access = buffer.bind()) {
            var buf = BufferUtils.createIntBuffer(World.BLOCKS_PER_CHUNK);
            access.copyDataTo(buf, 0);
        }
        super.addTerrainLayer(chunk);
    }
}