package app.block.model;

import org.joml.Vector3i;

public record BlockModel(
        PartialMesh top, PartialMesh front, PartialMesh right,
        PartialMesh bottom, PartialMesh back, PartialMesh left,
        PartialMesh inner) {

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
        public final Vector3i v00, v01, v10, v11, e0010, e1011, e1101, e0100;

        private static Vector3i cross(Vector3i a, Vector3i b) {
            return new Vector3i(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
            );
        }

        private static Vector3i avg(Vector3i a, Vector3i b){
            return new Vector3i().add(a).add(b).div(2);
        }

        FaceDirection(Vector3i direction, float lightMultiplier) {
            this.direction = direction;
            this.lightMultiplier = lightMultiplier;

            v00 = cross(this.direction, new Vector3i(1));
            v01 = cross(this.direction, v00);
            v11 = cross(this.direction, v01);
            v10 = cross(this.direction, v11);
            e0010 = avg(v00, v10);
            e1011 = avg(v10, v11);
            e1101 = avg(v11, v01);
            e0100 = avg(v01, v00);
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
}