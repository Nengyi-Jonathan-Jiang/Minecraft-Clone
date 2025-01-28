package j3d.graph.buffers;

import org.lwjgl.BufferUtils;
import util.Resource;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class ArrayBuffer implements Resource {
    public final int id;
    public final BufferType type;
    public final BufferUsage usage;

    public ArrayBuffer(BufferType type, BufferUsage usage) {
        this.type = type;
        this.usage = usage;
        id = glGenBuffers();
    }

    public enum BufferType {
        VertexBuffer(GL_ARRAY_BUFFER),
        IndexBuffer(GL_ELEMENT_ARRAY_BUFFER);

        private final int value;

        BufferType(int value) {
            this.value = value;
        }
    }

    public enum BufferUsage {
        StaticDraw(GL_STATIC_DRAW),
        DynamicDraw(GL_DYNAMIC_DRAW),
        StaticRead(GL_STATIC_READ),
        DynamicRead(GL_DYNAMIC_READ),
        StreamDraw(GL_STREAM_DRAW),
        StreamRead(GL_STREAM_READ);

        private final int value;

        BufferUsage(int value) {
            this.value = value;
        }
    }

    public void bind() {
        glBindBuffer(type.value, id);
    }

    public void setData(IntBuffer data) {
        if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
        glBindBuffer(type.value, id);
        glBufferData(type.value, data, usage.value);
    }

    public void setData(FloatBuffer data) {
        if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
        glBindBuffer(type.value, id);
        glBufferData(type.value, data, usage.value);
    }

    public void setData(ByteBuffer data) {
        if (!data.isDirect()) throw new IllegalStateException("Buffers passed to OpenGL must be direct");
        glBindBuffer(type.value, id);
        glBufferData(type.value, data, usage.value);
    }

    public void setData(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(0, data);
        setData(buffer);
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

    @Override
    public void freeResources() {
        glDeleteBuffers(id);
    }
}
