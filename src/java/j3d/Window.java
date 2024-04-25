package j3d;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Window {
    protected final long windowHandle;
    protected final Dimension dimensions;
    protected final MouseInput mouseInput;
    protected final Callable<Void> resizeFunc;
    protected final List<KeyListener> keyListeners = new ArrayList<>();
    protected final List<MouseListener> mouseListeners = new ArrayList<>();

    public Window(Callable<Void> resizeFunc, String title) {
        this.resizeFunc = resizeFunc;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        setWindowHints();

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

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
            keyListeners.forEach(listener -> listener.onKeyEvent(key, action));
        });
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods)-> {
            mouseListeners.forEach(listener -> listener.onMouseEvent(button, action));
        });

        glfwMakeContextCurrent(windowHandle);

        glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
        dimensions.width = arrWidth[0];
        dimensions.height = arrHeight[0];

        mouseInput = new MouseInput(this);
    }

    public abstract void setWindowHints();

    public void addKeyListener(KeyListener keyListener) {
        this.keyListeners.add(keyListener);
    }

    public void addMouseListener(MouseListener mouseListener) {
        this.mouseListeners.add(mouseListener);
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

    public void updateScreen() {
        glfwSwapBuffers(windowHandle);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public interface KeyListener {
        void onKeyEvent(int keyCode, int action);
    }

    public interface MouseListener {
        void onMouseEvent(int button, int action);
    }
}
