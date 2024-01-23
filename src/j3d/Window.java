package j3d;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final long windowHandle;
    private final Dimension dimensions;
    private final MouseInput mouseInput;
    private final Callable<Void> resizeFunc;

    public Window(String title, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vidMode != null;
        dimensions = new Dimension(vidMode.width(), vidMode.height());

        windowHandle = glfwCreateWindow(dimensions.width, dimensions.height, title, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> onResize(w, h));

        glfwSetErrorCallback((int errorCode, long msgPtr) ->
            System.out.printf("Error code [%s], msg [%s]%n", errorCode, MemoryUtil.memUTF8(msgPtr))
        );

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> keyCallBack(key, action));

        glfwMakeContextCurrent(windowHandle);

        glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
        dimensions.width = arrWidth[0];
        dimensions.height = arrHeight[0];

        mouseInput = new MouseInput(windowHandle);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public int getHeight() {
        return dimensions.height;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public int getWidth() {
        return dimensions.width;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    protected void onResize(int width, int height) {
        this.dimensions.width = width;
        this.dimensions.height = height;
        try {
            resizeFunc.call();
        } catch (Exception e) {
            System.out.println("Error calling resize callback" + e);
        }
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }
}
