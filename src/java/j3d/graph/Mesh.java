package j3d.graph;

import j3d.graph.buffers.ArrayBuffer;
import util.Resource;

import java.util.Arrays;

import static j3d.graph.buffers.ArrayBuffer.BufferType;
import static j3d.graph.buffers.ArrayBuffer.BufferUsage;
import static org.lwjgl.opengl.GL30.*;

public class Mesh implements Resource {
    private final int numIndices;

    // TODO: un-mix the levels of abstraction here. vaoID (low level) should not be mixed with
    //  ArrayBuffer (medium level)
    private final int vaoID;
    private final ArrayBuffer[] arrayBuffers;
    private final PrimitiveType primitiveType;

    public enum PrimitiveType {
        Points(GL_POINTS), LineStrip(GL_LINE_STRIP), LineLoop(GL_LINE_LOOP), Lines(GL_LINES),
        TriangleStrip(GL_TRIANGLE_STRIP), TriangleFan(GL_TRIANGLE_FAN), Triangles(GL_TRIANGLES);

        private final int value;

        PrimitiveType(int value) {
            this.value = value;
        }
    }

    public sealed interface MeshAttributeData permits FloatAttributeData, IntAttributeData {
        static MeshAttributeData create(int elementSize, float[] data) {
            return new FloatAttributeData(elementSize, data);
        }

        static MeshAttributeData create(int elementSize, int[] data) {
            return new IntAttributeData(elementSize, data);
        }

        void writeToBuffer(ArrayBuffer buffer);

        void configureVertexArray(int attributeIndex);
    }

    public record FloatAttributeData(int elementSize, float[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(ArrayBuffer buffer) {
            buffer.setData(data);
        }

        @Override
        public void configureVertexArray(int attributeIndex) {
            glEnableVertexAttribArray(attributeIndex);
            glVertexAttribPointer(attributeIndex, elementSize, GL_FLOAT, false, 0, 0);
        }
    }

    public record IntAttributeData(int elementSize, int[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(ArrayBuffer buffer) {
            buffer.setData(data);
        }

        @Override
        public void configureVertexArray(int attributeIndex) {
            glEnableVertexAttribArray(attributeIndex);
            glVertexAttribPointer(attributeIndex, elementSize, GL_INT, false, 0, 0);
        }
    }

    public Mesh(int[] indices, MeshAttributeData... attributeData) {
        this(PrimitiveType.Triangles, indices, attributeData);
    }

    public Mesh(PrimitiveType primitiveType, int[] indices, MeshAttributeData... attributeData) {
        numIndices = indices.length;
        this.primitiveType = primitiveType;
        arrayBuffers = new ArrayBuffer[attributeData.length + 1];

        // Create VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create VBOs
        int index;
        for (index = 0; index < attributeData.length; index++) {
            MeshAttributeData dat = attributeData[index];
            ArrayBuffer buffer = arrayBuffers[index] = new ArrayBuffer(BufferType.VertexBuffer, BufferUsage.DynamicDraw);

            dat.writeToBuffer(buffer);
            dat.configureVertexArray(index);
        }

        // Create index buffer
        ArrayBuffer indexBuffer = arrayBuffers[index] = new ArrayBuffer(BufferType.IndexBuffer, BufferUsage.DynamicDraw);
        indexBuffer.setData(indices);
    }

    public void freeResources() {
        Arrays.stream(arrayBuffers).forEach(ArrayBuffer::freeResources);
        glDeleteVertexArrays(vaoID);
    }

    public int getNumIndices() {
        return numIndices;
    }

    public final int getVaoID() {
        return vaoID;
    }

    public void draw() {
        glBindVertexArray(vaoID);
        glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);
    }
}
