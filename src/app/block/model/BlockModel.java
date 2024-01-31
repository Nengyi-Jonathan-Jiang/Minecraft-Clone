package app.block.model;

import org.joml.Vector3i;

public record BlockModel(
        PartialMesh top, PartialMesh front, PartialMesh right,
        PartialMesh bottom, PartialMesh back, PartialMesh left,
        PartialMesh inner) {

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

    public PartialMesh getFace(FaceDirection direction) {
        return switch (direction) {
            case TOP -> top;
            case FRONT -> front;
            case RIGHT -> right;
            case BOTTOM -> bottom;
            case BACK -> back;
            case LEFT -> left;
            case INNER -> inner;
        };
    }

    public enum FaceDirection {
        TOP(new Vector3i(0, 1, 0), 1),
        FRONT(new Vector3i(0, 0, 1), .7f),
        RIGHT(new Vector3i(1, 0, 0), .7f),
        BOTTOM(new Vector3i(0, -1, 0), .5f),
        BACK(new Vector3i(0, 0, -1), .7f),
        LEFT(new Vector3i(-1, 0, 0), .7f),
        INNER(new Vector3i(0, 0, 0), .6f);

        public static final FaceDirection[] OUTER_FACES = {TOP, FRONT, RIGHT, BOTTOM, BACK, LEFT, INNER};

        public final Vector3i direction;
        public final float lightMultiplier;
        public final Vector3i T1, T2, t1, t2;

        FaceDirection(Vector3i direction, float lightMultiplier) {
            this.direction = direction;
            this.lightMultiplier = lightMultiplier;

            // Intellij doesn't like it when I permute the arguments.

            //noinspection SuspiciousNameCombination
            T1 = new Vector3i(direction.y, direction.z, direction.x);
            t1 = T1.mul(-1, new Vector3i());
            //noinspection SuspiciousNameCombination
            T2 = new Vector3i(direction.z, direction.x, direction.y);
            t2 = T2.mul(-1, new Vector3i());
        }
    }
}