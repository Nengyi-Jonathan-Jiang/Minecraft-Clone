package j3d.graph;

import util.Resource;

import static org.lwjgl.opengl.GL43.*;

public class VertexArrayObject implements Resource {
    public final int id;

    public VertexArrayObject() {
        id = glGenVertexArrays();
    }

    @Override
    public void freeResources() {
        glDeleteVertexArrays(id);
    }

    public VertexArrayObjectAccess bind() {
        return new VertexArrayObjectAccess();
    }

    public enum ShapePrimitiveType {
        Points(GL_POINTS), LineStrip(GL_LINE_STRIP), LineLoop(GL_LINE_LOOP), Lines(GL_LINES),
        TriangleStrip(GL_TRIANGLE_STRIP), TriangleFan(GL_TRIANGLE_FAN), Triangles(GL_TRIANGLES);

        public final int glEnumValue;

        ShapePrimitiveType(int glEnumValue) {
            this.glEnumValue = glEnumValue;
        }
    }

    @SuppressWarnings("unused")
    public class VertexArrayObjectAccess implements AutoCloseable {
        private VertexArrayObjectAccess() {
            glBindVertexArray(id);
        }

        public void setAttributePointer(GPUBuffer.BufferAccess __, int attributeIndex, GPUPrimitiveType.ScalarType type, int elementSize) {
            glEnableVertexAttribArray(attributeIndex);
            glVertexAttribPointer(attributeIndex, elementSize, type.glEnumValue, false, 0, 0);
        }

        @SuppressWarnings("resource")
        public void setIndexBuffer(GPUBuffer buffer) {
            if (!(buffer.bufferType == GPUBuffer.BufferType.IndexBuffer)) {
                throw new IllegalArgumentException("Index buffer is not a IndexBuffer");
            }
            buffer.bind();
        }

        public void draw(ShapePrimitiveType shapePrimitiveType, int count) {
            glDrawArrays(shapePrimitiveType.glEnumValue, 0, count);
        }

        public void drawIndexed(ShapePrimitiveType shapePrimitiveType, int count) {
            glDrawElements(shapePrimitiveType.glEnumValue, count, GL_UNSIGNED_INT, 0);
        }

        @Override
        public void close() {
            glBindVertexArray(0);
        }
    }
}
