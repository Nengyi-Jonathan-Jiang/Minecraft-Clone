package j3d.graph;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private final int numIndices;
    private final int vaoId;
    private final List<Integer> vboIdList;

    public sealed interface MeshAttributeData permits FloatAttributeData, IntAttributeData {
        static MeshAttributeData create(int elementSize, float[] data) {
            return new FloatAttributeData(elementSize, data);
        }
        static MeshAttributeData create(int elementSize, int[] data) {
            return new IntAttributeData(elementSize, data);
        }
    }
    public record FloatAttributeData (int elementSize, float[] data) implements MeshAttributeData {}
    public record IntAttributeData  (int elementSize, int[] data) implements MeshAttributeData {}

    public Mesh(int[] indices, MeshAttributeData... data) {
        numIndices = indices.length;
        vboIdList = new ArrayList<>();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        int vboId;

        // TODO: make this better code.
        for(int idx = 0; idx < data.length; idx++) {

            MeshAttributeData dat = data[idx];
            if(dat instanceof FloatAttributeData d) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                FloatBuffer buffer = BufferUtils.createFloatBuffer(d.data.length);
                buffer.put(0, d.data);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(idx);
                glVertexAttribPointer(idx, d.elementSize, GL_FLOAT, false, 0, 0);
            }
            else if(dat instanceof IntAttributeData d) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                IntBuffer buffer = BufferUtils.createIntBuffer(d.data.length);
                buffer.put(0, d.data);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(idx );
                glVertexAttribPointer(idx, d.elementSize, GL_INT, false, 0, 0);
            }
            else {
                throw new RuntimeException("What the hell happened here? I thought I sealed the class...");
            }
        }

        // Index VBO
        vboId = glGenBuffers();
        vboIdList.add(vboId);
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(0, indices);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        vboIdList.forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoId);
    }

    public int getNumIndices() {
        return numIndices;
    }

    public final int getVaoId() {
        return vaoId;
    }
}
