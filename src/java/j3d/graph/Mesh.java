package j3d.graph;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import util.Resource;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL30.*;

public class Mesh implements Resource {
    private final int numIndices;
    private final int vaoID;
    private final int[] vboIDList;

    public sealed interface MeshAttributeData permits FloatAttributeData, IntAttributeData {
        static MeshAttributeData create(int elementSize, float[] data) {
            return new FloatAttributeData(elementSize, data);
        }

        static MeshAttributeData create(int elementSize, int[] data) {
            return new IntAttributeData(elementSize, data);
        }

        void writeToBuffer(int vboID);

        void configureVertexArray(int attributeIndex);
    }

    public record FloatAttributeData(int elementSize, float[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(int vboID) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
            buffer.put(0, data);
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
        }

        @Override
        public void configureVertexArray(int attributeIndex) {
            glVertexAttribPointer(attributeIndex, elementSize, GL_FLOAT, false, 0, 0);
        }
    }

    public record IntAttributeData(int elementSize, int[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(int vboID) {
            IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
            buffer.put(0, data);
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
        }

        @Override
        public void configureVertexArray(int attributeIndex) {
            glVertexAttribPointer(attributeIndex, elementSize, GL_INT, false, 0, 0);
        }

    }

    public Mesh(int[] indices, MeshAttributeData... attributeData) {
        numIndices = indices.length;
        vboIDList = new int[attributeData.length + 1];

        // Create VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create VBOs
        int vboID;
        for (int index = 0; index < attributeData.length; index++) {
            MeshAttributeData dat = attributeData[index];

            vboID = glGenBuffers();
            vboIDList[index] = vboID;
            dat.writeToBuffer(vboID);
            glEnableVertexAttribArray(index);
            dat.configureVertexArray(index);
        }

        // Create EBO
        vboID = glGenBuffers();
        vboIDList[attributeData.length] = vboID;
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(numIndices);
        indicesBuffer.put(0, indices);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_DYNAMIC_DRAW);

        // Unbind everything
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void freeResources() {
        Arrays.stream(vboIDList).forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoID);
    }

    public int getNumIndices() {
        return numIndices;
    }

    public final int getVaoID() {
        return vaoID;
    }
}
