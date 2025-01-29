package j3d.graph;

import j3d.graph.GPUBuffer.BufferType;
import util.Resource;

import java.util.Arrays;

import static j3d.graph.GPUBuffer.BufferUsage;

public class Mesh implements Resource {
    private final int numIndices;

    // TODO: un-mix the levels of abstraction here. vaoID (low level) should not be mixed with
    //  ArrayBuffer (medium level)
    private final VertexArrayObject vertexArrayObject;
    private final GPUBuffer[] buffers;
    private final VertexArrayObject.ShapePrimitiveType shapePrimitiveType;

    public sealed interface MeshAttributeData permits FloatAttributeData, IntAttributeData {
        static MeshAttributeData create(int elementSize, float[] data) {
            return new FloatAttributeData(elementSize, data);
        }

        static MeshAttributeData create(int elementSize, int[] data) {
            return new IntAttributeData(elementSize, data);
        }

        void writeToBuffer(GPUBuffer.BufferAccess access);

        void configureVertexArray(VertexArrayObject.VertexArrayObjectAccess vao, int attributeIndex, GPUBuffer.BufferAccess buffer);
    }

    public record FloatAttributeData(int elementSize, float[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(GPUBuffer.BufferAccess access) {
            access.setData(data);
        }

        @Override
        public void configureVertexArray(VertexArrayObject.VertexArrayObjectAccess vao, int attributeIndex, GPUBuffer.BufferAccess buffer) {
            vao.setAttributePointer(buffer, attributeIndex, GPUPrimitiveType.ScalarType.Float, elementSize);
        }
    }

    public record IntAttributeData(int elementSize, int[] data) implements MeshAttributeData {
        @Override
        public void writeToBuffer(GPUBuffer.BufferAccess access) {
            access.setData(data);
        }

        @Override
        public void configureVertexArray(VertexArrayObject.VertexArrayObjectAccess vao, int attributeIndex, GPUBuffer.BufferAccess buffer) {
            vao.setAttributePointer(buffer, attributeIndex, GPUPrimitiveType.ScalarType.Int, elementSize);
        }
    }

    public Mesh(int[] indices, MeshAttributeData... attributeData) {
        this(VertexArrayObject.ShapePrimitiveType.Triangles, indices, attributeData);
    }

    public Mesh(VertexArrayObject.ShapePrimitiveType shapePrimitiveType, int[] indices, MeshAttributeData... attributeData) {
        numIndices = indices.length;
        this.shapePrimitiveType = shapePrimitiveType;
        buffers = new GPUBuffer[attributeData.length + 1];

        // Create VAO
        vertexArrayObject = new VertexArrayObject();

        try (var vaoAccess = vertexArrayObject.bind()) {

            // Create VBOs
            int index;
            for (index = 0; index < attributeData.length; index++) {
                MeshAttributeData dat = attributeData[index];
                GPUBuffer buffer = buffers[index] = new GPUBuffer(BufferType.ArrayBuffer, BufferUsage.DynamicDraw);

                try (var bufferAccess = buffer.bind()) {
                    dat.writeToBuffer(bufferAccess);
                    dat.configureVertexArray(vaoAccess, index, bufferAccess);
                }
            }

            // Create index buffer
            GPUBuffer indexBuffer = buffers[index] = new GPUBuffer(BufferType.IndexBuffer, BufferUsage.DynamicDraw);

            try (var bufferAccess = indexBuffer.bind()) {
                bufferAccess.setData(indices);
            }

            vaoAccess.setIndexBuffer(indexBuffer);
        }
    }

    public void freeResources() {
        Arrays.stream(buffers).forEach(GPUBuffer::freeResources);
        vertexArrayObject.freeResources();
    }

    public void draw() {
        try (var access = vertexArrayObject.bind()) {
            access.drawIndexed(shapePrimitiveType, numIndices);
        }
    }
}
