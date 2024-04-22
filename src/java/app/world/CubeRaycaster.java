package app.world;

import app.util.Vec3i;
import app.util.WorldPosition;
import j3d.scene.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;
import util.MathUtil;

import java.util.Iterator;

public class CubeRaycaster {
    public Iterator<WorldPosition> cast(Camera camera) {
        Vector3f currPos = camera.getPosition();
        Vector2f cameraRotation = camera.getRotation();

        Vector3f start = new Vector3f(.5f).add(currPos);
        Vector3f stepper = new Vector3f(0, 0, -1).rotateX(-cameraRotation.x).rotateY(-cameraRotation.y);
        Vector3f prev = new Vector3f(start);
        
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public WorldPosition next() {
                float step = MathUtil.min(
                        (1 - (MathUtil.mod(start.x * Math.signum(stepper.x), 1))) / Math.abs(stepper.x),
                        (1 - (MathUtil.mod(start.y * Math.signum(stepper.y), 1))) / Math.abs(stepper.y),
                        (1 - (MathUtil.mod(start.z * Math.signum(stepper.z), 1))) / Math.abs(stepper.z)
                );

                prev.set(start);
                start.add(new Vector3f(stepper).mul(step));

                Vector3f result = new Vector3f(start.add(prev, new Vector3f()).mul(.5f)).floor();

                return new WorldPosition((int) result.x, (int) result.y, (int) result.z);
            }
        };
    }
}
