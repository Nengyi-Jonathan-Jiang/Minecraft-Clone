package j3d.opengl;


import j3d.Engine;
import j3d.IAppLogic;
import j3d.Window;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import util.Resource;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.WGL.wglGetCurrentContext;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLEngine extends Engine implements Resource {
    private final IAppLogic appLogic;

    public OpenGLEngine(String windowTitle, Function<Window, IAppLogic> appLogicConstructor) {
        super(windowTitle, OpenGLWindow::new);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);

        appLogic = appLogicConstructor.apply(window);
        running = true;
    }

    private static final Queue<AtomicLong> awaitingContexts = new ArrayDeque<>();
    public static long mainThreadID;

    public static boolean isGLInitialized() {
        return wglGetCurrentContext() != 0;
    }

    private static long initializeGLFWHeadless() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        long windowHandle = glfwCreateWindow(1, 1, "Headless OpenGL", NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetErrorCallback((int errorCode, long msgPtr) -> System.out.printf("Error code [%s], msg [%s]%n", errorCode, MemoryUtil.memUTF8(msgPtr)));

        return windowHandle;
    }

    public static void initializeHeadless() {
        if (isGLInitialized()) {
            return;
        }

        long windowHandle;
        if (Thread.currentThread().threadId() == mainThreadID) {
            System.out.println("Initializing Headless OpenGL on main thread");
            windowHandle = initializeGLFWHeadless();
        } else {
            System.out.println("Initializing Headless OpenGL on thread " + Thread.currentThread().threadId());
            AtomicLong atomicWindowHandle = new AtomicLong(-1);
            synchronized (awaitingContexts) {
                awaitingContexts.offer(atomicWindowHandle);
            }
            while (atomicWindowHandle.get() == -1) {
                Thread.onSpinWait();
            }
            windowHandle = atomicWindowHandle.get();
            System.out.println("Done initializing Headless OpenGL thread " + Thread.currentThread().threadId());
        }
        glfwMakeContextCurrent(windowHandle);

        GL.createCapabilities();
    }

    public static void terminateAll() {
        glfwTerminate();
    }

    @Override
    public void freeResources() {
        appLogic.freeResources();
        window.freeResources();
    }

    @Override
    public void resize() {
        appLogic.resize(window.getWidth(), window.getHeight());
    }

    @Override
    protected void draw(Window window) {
        appLogic.draw(window);
    }

    @Override
    protected void always() {
        if (Thread.currentThread().threadId() == mainThreadID) {
            synchronized (awaitingContexts) {
                while (!awaitingContexts.isEmpty()) {
                    AtomicLong awaitingContext = awaitingContexts.poll();
                    long windowHandle = initializeGLFWHeadless();
                    awaitingContext.set(windowHandle);
                }
            }
        }
    }

    @Override
    protected void update(Window window, int elapsedTimeMillis) {
        appLogic.update(window, elapsedTimeMillis);
    }

    @Override
    protected void input(Window window, int elapsedTimeMillis) {
        appLogic.input(window, elapsedTimeMillis);
    }
}
