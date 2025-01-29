package j3d.graph;

import static org.lwjgl.opengl.GL43.*;

@SuppressWarnings("unused")
public final class GPUPrimitiveType {
    private GPUPrimitiveType() {}

    public enum ScalarType {
        Byte(GL_BYTE), Short(GL_SHORT), Int(GL_INT), Float(GL_FLOAT);

        public final int glEnumValue;

        ScalarType(int glEnumValue) {this.glEnumValue = glEnumValue;}
    }

    public enum VectorSize {
        _1(1), _2(2), _3(3), _4(4);

        public final int size;

        VectorSize(int size) {
            this.size = size;
        }
    }

    public record VectorType(ScalarType scalarType, VectorSize vectorSize) {}

    public record MatrixType(ScalarType scalarType, VectorSize rows, VectorSize columns) {}
}
