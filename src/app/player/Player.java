package app.player;

import j3d.scene.Camera;
import org.joml.Vector3f;

public class Player {
    private Vector3f velocity;
    private final Camera camera;

    public Player() {
        this.camera = new Camera();
    }

    public void setPosition(Vector3f position) {
        camera.setPosition(position.x, position.y, position.z);
    }

    public Vector3f getPosition() {
        return camera.getPosition();
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Camera getCamera() {
        return camera;
    }
}
