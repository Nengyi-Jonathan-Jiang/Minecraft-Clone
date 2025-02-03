package app.world.worldgen;

import app.block.Block;
import app.block.BlockRegistry;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.util.PositionInChunk;
import j3d.graph.GPUBuffer;
import j3d.graph.GPUBuffer.BufferAccess.ImmutableStorageAccessFlags;
import j3d.graph.Shader;
import j3d.opengl.OpenGLEngine;
import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static j3d.graph.GPUBuffer.BufferType;
import static j3d.graph.GPUBuffer.BufferUsage;
import static org.lwjgl.opengl.GL43.*;

// TODO: run on GPU
public class GPUWorldGenerator implements WorldGenerator {
    private static class GPUWorldGeneratorContext {
        private final Shader shader;
        private final GPUBuffer ssbo;
        private final IntBuffer ssboOutputBuffer;
        private final ReentrantLock lock = new ReentrantLock();

        {
            OpenGLEngine.initializeHeadless();

            this.shader = new Shader(new Shader.ShaderModuleData(
                "shaders/compute/basic_terrain.glsl",
                Shader.ShaderType.Compute
            ));

            ssboOutputBuffer = BufferUtils.createIntBuffer(World.BLOCKS_PER_CHUNK);
            ssbo = new GPUBuffer(
                BufferType.ShaderStorageBuffer,
                BufferUsage.StaticRead
            );
            try (var access = ssbo.bind()) {
                access.allocateImmutably(
                    World.BLOCKS_PER_CHUNK * 4,
                    ImmutableStorageAccessFlags.Read | ImmutableStorageAccessFlags.SubData
                );
            }
        }
    }

    private final Map<Long, GPUWorldGeneratorContext> contextMap = new HashMap<>();

    public GPUWorldGenerator() {
    }

    @Override
    public Chunk generateChunk(ChunkOffset chunkOffset, World world) {
        GPUWorldGeneratorContext context = contextMap.computeIfAbsent(Thread.currentThread().threadId(), ignored -> new GPUWorldGeneratorContext());

        Chunk result = new Chunk(chunkOffset, world);
        addTerrainLayer(result, context);
        convertTopLayersToGrass(result, context);
        addBedrockLayer(result, context);

        result.getLightingData().invalidateAll();

        return result;
    }

    private void convertTopLayersToGrass(Chunk result, GPUWorldGeneratorContext context) {
        for (PositionInChunk pos : PositionInChunk.allPositionsInChunk()) {
            Block thisBlock = result.getBlockAt(pos);

            if (thisBlock == null) continue;

            if (thisBlock.name.equals("dirt")) {
                if (pos.y() == World.CHUNK_HEIGHT - 1 || result.getBlockIDAt(pos.add(0, 1, 0, new PositionInChunk())) == 0) {
                    result.setBlockAt(pos, BlockRegistry.getBlockID("grass"), false);
                }
            }
        }
    }

    private void addBedrockLayer(Chunk chunk, GPUWorldGeneratorContext context) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                chunk.setBlockAt(new PositionInChunk(x, 0, z), BlockRegistry.getBlockID("bedrock"), false);
            }
        }
    }

    private void addTerrainLayer(Chunk chunk, GPUWorldGeneratorContext context) {
        var lock = context.lock;
        var shader = context.shader;
        var ssbo = context.ssbo;
        var ssboOutputBuffer = context.ssboOutputBuffer;

        lock.lock();
        shader.bind();
        try (var ignored1 = ssbo.bindToShader(0)) {
            shader.uniforms().setUniform("chunkOffset", chunk.getChunkOffset());
            glDispatchCompute(World.CHUNK_SIZE, World.CHUNK_HEIGHT, World.CHUNK_SIZE);
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
        }
        try (var access = ssbo.bind()) {
            access.copyDataTo(ssboOutputBuffer, 0);
        }
        for (PositionInChunk pos : PositionInChunk.allPositionsInChunk()) {
            chunk.setBlockAt(pos, ssboOutputBuffer.get(pos.getBits()), false);
        }
        shader.unbind();
        lock.unlock();
    }
}