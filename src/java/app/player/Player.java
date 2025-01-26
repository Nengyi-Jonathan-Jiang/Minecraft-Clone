package app.player;

import j3d.scene.Camera;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final Camera camera;
    private Vector3f velocity;

    public final Cooldown
        placeBlockCooldown = new Cooldown(100),
        deleteBlockCooldown = new Cooldown(100);

    public Player() {
        this.camera = new Camera();
    }

    public void update(int elapsedTimeMillis) {
        placeBlockCooldown.update(elapsedTimeMillis);
        deleteBlockCooldown.update(elapsedTimeMillis);
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

    public void move(Vector3f movement) {
        camera.move(movement);
    }

    public void setRotation(float x, float y) {
        camera.setRotation(x, y);
    }

    public Vector2f getRotation() {
        return camera.getRotation();
    }

    public void rotateBy(float x, float y) {
        camera.rotateBy(x, y);
    }
}
