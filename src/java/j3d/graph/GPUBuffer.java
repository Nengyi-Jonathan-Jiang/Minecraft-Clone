package j3d.graph;

import org.lwjgl.BufferUtils;
import util.NotThreadSafe;
import util.Resource;
import util.SimpleAutoCloseable;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL44.*;

@NotThreadSafe
public final class GPUBuffer implements Resource {
    public final int id;
    public final BufferType bufferType;
    public final BufferUsage usage;

    public GPUBuffer(BufferType type, BufferUsage usage) {
        this.bufferType = type;
        this.usage = usage;
        id = glGenBuffers();
    }

    public enum BufferType {
        ArrayBuffer(GL_ARRAY_BUFFER),
        IndexBuffer(GL_ELEMENT_ARRAY_BUFFER),
        UniformBuffer(GL_UNIFORM_BUFFER),
        ShaderStorageBuffer(GL_SHADER_STORAGE_BUFFER);

        public final int glEnumValue;

        BufferType(int glEnumValue) {
            this.glEnumValue = glEnumValue;
        }
    }

    public enum BufferUsage {
        StaticDraw(GL_STATIC_DRAW), DynamicDraw(GL_DYNAMIC_DRAW), StreamDraw(GL_STREAM_DRAW), StaticRead(GL_STATIC_READ), DynamicRead(GL_DYNAMIC_READ), StreamRead(GL_STREAM_READ), StaticCopy(GL_STATIC_COPY), DynamicCopy(GL_DYNAMIC_COPY), StreamCopy(GL_STREAM_COPY);

        private final int value;

        BufferUsage(int value) {
            this.value = value;
        }
    }

    public enum GetBufferDataUsage {
        Read(GL_MAP_READ_BIT), Write(GL_MAP_WRITE_BIT), ReadWrite(GL_MAP_READ_BIT | GL_MAP_WRITE_BIT);

        private final int value;

        GetBufferDataUsage(int value) {
            this.value = value;
        }
    }

    @SuppressWarnings("unused")
    public final class BufferAccess implements AutoCloseable {
        private BufferAccess() {
            glBindBuffer(bufferType.glEnumValue, id);
        }

        @Override
        public void close() {
            glBindBuffer(bufferType.glEnumValue, 0);
        }

        public GPUBuffer getBuffer() {
            return GPUBuffer.this;
        }

        public void copyDataTo(IntBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glGetBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public void copyDataTo(FloatBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glGetBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public void copyDataTo(ByteBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glGetBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public static class ImmutableStorageAccessFlags {
            public static final int Read = GL_MAP_READ_BIT;
            public static final int Write = GL_MAP_WRITE_BIT;
            public static final int SubData = GL_DYNAMIC_STORAGE_BIT;
            public static final int Persistent = GL_MAP_PERSISTENT_BIT;
            public static final int Coherent = GL_MAP_COHERENT_BIT;
        }

        public void allocateImmutably(int numBytes, int flags) {
            glBufferStorage(bufferType.glEnumValue, numBytes, flags);
        }

        public void setData(IntBuffer data) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferData(bufferType.glEnumValue, data, usage.value);
        }

        public void setData(FloatBuffer data) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferData(bufferType.glEnumValue, data, usage.value);
        }

        public void setData(ByteBuffer data) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferData(bufferType.glEnumValue, data, usage.value);
        }

        public void subData(IntBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public void subData(FloatBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public void subData(ByteBuffer data, int byteOffset) {
            if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
            glBufferSubData(bufferType.glEnumValue, byteOffset, data);
        }

        public void setData(int[] data) {
            synchronized (temporaryBuffer) {
                if (temporaryBuffer == null || data.length * 4 >= temporaryBuffer.capacity()) {
                    temporaryBuffer = BufferUtils.createByteBuffer(data.length * 4);
                }
                temporaryBuffer.asIntBuffer().put(0, data);
                setData(temporaryBuffer.slice(0, data.length));
            }
        }

        public void setData(float[] data) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
            buffer.put(0, data);
            setData(buffer);
        }

        public void setData(byte[] data) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(0, data);
            setData(buffer);
        }

        public void subData(int[] data, int byteOffset) {
            IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
            buffer.put(0, data);
            subData(buffer, byteOffset);
        }

        public void subData(float[] data, int byteOffset) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
            buffer.put(0, data);
            subData(buffer, byteOffset);
        }

        public void subData(byte[] data, int byteOffset) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(0, data);
            subData(buffer, byteOffset);
        }

        public RawBufferDataAccess rawAccess(int numBytes, int byteOffset, GetBufferDataUsage usage) {
            ByteBuffer result = BufferUtils.createByteBuffer(numBytes);
            glMapBufferRange(bufferType.glEnumValue, byteOffset, numBytes, usage.value);
            return new RawBufferDataAccess(result);
        }

        public final class RawBufferDataAccess implements AutoCloseable {
            public final ByteBuffer buffer;

            private RawBufferDataAccess(ByteBuffer buffer) {
                this.buffer = buffer;
            }

            @Override
            public void close() {
                glUnmapBuffer(bufferType.glEnumValue);
            }
        }

        private static final AtomicReference<ByteBuffer> temporaryBuffer = new AtomicReference<>(null);
    }

    public BufferAccess bind() {
        return new BufferAccess();
    }

    public SimpleAutoCloseable bindToShader(int bindingIndex) {
        switch (bufferType) {
            case UniformBuffer, ShaderStorageBuffer -> {
            }
            default -> throw new IllegalArgumentException(
                "Only uniform buffers or shader storage buffers may be bound to a shader"
            );
        }

        glBindBufferBase(bufferType.glEnumValue, bindingIndex, id);
        return new SimpleAutoCloseable() {
            @Override
            public void close() {
                glBindBufferBase(bufferType.glEnumValue, bindingIndex, 0);
            }
        };
    }

    @Override
    public void freeResources() {
        glDeleteBuffers(id);
    }
}
