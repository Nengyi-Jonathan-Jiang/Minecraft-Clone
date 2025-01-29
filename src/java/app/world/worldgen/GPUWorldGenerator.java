package app.world.worldgen;

import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import j3d.graph.GPUBuffer;
import j3d.graph.Shader;
import j3d.opengl.OpenGLEngine;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL43.*;

// TODO: run on GPU
public class GPUWorldGenerator implements WorldGenerator {
    private final Shader shader;
    private final DefaultWorldGenerator fallback = new DefaultWorldGenerator();

    public GPUWorldGenerator() {
        this.shader = new Shader(new Shader.ShaderModuleData(
            "shaders/compute/basic_terrain.glsl",
            Shader.ShaderType.Compute
        ));

        var buf = new GPUBuffer(GPUBuffer.BufferType.ShaderStorageBuffer, GPUBuffer.BufferUsage.DynamicDraw);
        try (var access = buf.bind()) {
            access.setData(new int[World.BLOCKS_PER_CHUNK]);
        }

        shader.bind();
        try (var ignored = buf.bindToShader(0)) {
            glDispatchCompute(World.CHUNK_SIZE, World.CHUNK_HEIGHT, World.CHUNK_SIZE);
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        }
        try (var access = buf.bind()) {
            var out = BufferUtils.createIntBuffer(World.BLOCKS_PER_CHUNK);
            access.copyDataTo(out, 0);
            System.out.println(out.get(4));
            System.out.println(out.get(200));
        }
    }

    @Override
    public Chunk generateChunk(ChunkOffset chunkOffset, World world) {
        if (!OpenGLEngine.isGLInitialized()) {
            System.out.println("Initializing OpenGLContext");
            OpenGLEngine.initializeHeadless();
        }
        return fallback.generateChunk(chunkOffset, world);
    }
}