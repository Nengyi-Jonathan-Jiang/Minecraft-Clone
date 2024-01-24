package j3d.graph;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private final int numVertices;
    private final int vaoId;
    private final List<Integer> vboIdList;

    public sealed interface MeshAttributeData permits FloatAttributeData, IntAttributeData {}
    public record FloatAttributeData (int elementSize, float[] data) implements MeshAttributeData {}
    public record IntAttributeData  (int elementSize, int[] data) implements MeshAttributeData {}

    public Mesh(float[] positions, float[] textureCoordinates, int[] indices, MeshAttributeData... data) {
        numVertices = indices.length;
        vboIdList = new ArrayList<>();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Positions VBO
        int vboId = glGenBuffers();
        vboIdList.add(vboId);

        FloatBuffer positionsBuffer = BufferUtils.createFloatBuffer(positions.length);
        positionsBuffer.put(0, positions);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // Texture coordinates VBO
        vboId = glGenBuffers();
        vboIdList.add(vboId);
        FloatBuffer textCoordsBuffer = BufferUtils.createFloatBuffer(textureCoordinates.length);
        textCoordsBuffer.put(0, textureCoordinates);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

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
                glEnableVertexAttribArray(idx + 2);
                glVertexAttribPointer(idx + 2, d.elementSize, GL_FLOAT, false, 0, 0);
            }
            else if(dat instanceof IntAttributeData d) {
                vboId = glGenBuffers();
                vboIdList.add(vboId);
                IntBuffer buffer = BufferUtils.createIntBuffer(d.data.length);
                buffer.put(0, d.data);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
                glEnableVertexAttribArray(idx + 2);
                glVertexAttribPointer(idx + 2, d.elementSize, GL_INT, false, 0, 0);
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

    public int getNumVertices() {
        return numVertices;
    }

    public final int getVaoId() {
        return vaoId;
    }
}
