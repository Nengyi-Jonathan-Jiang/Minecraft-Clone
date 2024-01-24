package app.block.model;

import org.joml.Vector3i;

public record BlockModel(
        PartialMesh top, PartialMesh front, PartialMesh right,
        PartialMesh bottom, PartialMesh back, PartialMesh left,
        PartialMesh inner) {

    public enum FaceDirection {
        TOP(new Vector3i(0, 1, 0)),
        FRONT(new Vector3i(0, 0, 1)),
        RIGHT(new Vector3i(1, 0, 0)),
        BOTTOM(new Vector3i(0, -1, 0)),
        BACK(new Vector3i(0, 0, -1)),
        LEFT(new Vector3i(-1, 0, 0)),
        INNER(new Vector3i(0, 0, 0));

        public final Vector3i direction;

        FaceDirection(Vector3i direction) {
            this.direction = direction;
        }
    }

    public static BlockModel combine(BlockModel a, BlockModel b) {
        return new BlockModel(
                PartialMesh.combine(a.top, b.top),
                PartialMesh.combine(a.front, b.front),
                PartialMesh.combine(a.right, b.right),
                PartialMesh.combine(a.bottom, b.bottom),
                PartialMesh.combine(a.back, b.back),
                PartialMesh.combine(a.left, b.left),
                PartialMesh.combine(a.inner, b.inner)
        );
    }
}