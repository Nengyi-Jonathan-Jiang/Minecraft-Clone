package app.player;

import j3d.scene.Camera;
import org.joml.Vector3f;

public class Player {
    private final Camera camera;
    private Vector3f velocity;

    public Player() {
        this.camera = new Camera();
    }

    public Vector3f getPosition() {
        return camera.getPosition();
    }

    public void setPosition(Vector3f position) {
        camera.setPosition(position.x, position.y, position.z);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public Camera getCamera() {
        return camera;
    }
}
