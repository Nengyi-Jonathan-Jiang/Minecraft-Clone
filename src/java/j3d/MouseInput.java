package j3d;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    private final Vector2f currentPos;
    private final Vector2f movement;
    private boolean leftButtonPressed;
    private final Vector2f previousPos;
    private boolean rightButtonPressed;

    @SuppressWarnings("resource")
    public MouseInput(Window window) {
        previousPos = new Vector2f();
        currentPos = new Vector2f();
        movement = new Vector2f();
        leftButtonPressed = false;
        rightButtonPressed = false;

        glfwSetCursorPosCallback(window.getWindowHandle(), (handle, xpos, ypos) -> {
            currentPos.x = (float) xpos;
            currentPos.y = (float) ypos;
        });
        window.addMouseListener((button, action) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    public Vector2f getCurrentPos() {
        return currentPos;
    }

    public Vector2f getMovement() {
        return movement;
    }

    public void input() {
        movement.y = currentPos.x - previousPos.x;
        movement.x = currentPos.y - previousPos.y;

        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }
}
