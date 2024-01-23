package app.block.model;

public class PartialMesh {
    private final PartialMeshVertex[] vertices;
    private final int[] indices;

    public static final PartialMesh emptyMesh = new PartialMesh(new PartialMeshVertex[0], new int[0]);

    public PartialMesh(PartialMeshVertex[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public static PartialMesh combine(PartialMesh a, PartialMesh b) {
        final PartialMeshVertex[] vertices = new PartialMeshVertex[a.vertices.length + b.vertices.length];
        System.arraycopy(a.vertices, 0, vertices, 0, a.vertices.length);
        System.arraycopy(a.vertices, 0, vertices, a.vertices.length, b.vertices.length);

        final int[] indices = new int[a.indices.length + b.indices.length];
        System.arraycopy(a.indices, 0, indices, 0, a.indices.length);
        System.arraycopy(a.indices, 0, indices, a.indices.length, b.indices.length);

        return new PartialMesh(vertices, indices);
    }

    public PartialMeshVertex[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }
}