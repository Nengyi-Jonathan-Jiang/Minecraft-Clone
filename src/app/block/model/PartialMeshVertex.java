package app.block.model;

import org.joml.Vector3f;

public record PartialMeshVertex(float x, float y, float z, float tx, float ty) {
    public Vector3f pos() {
        return new Vector3f(x, y, z);
    }
}
