package j3d.opengl;


import j3d.Engine;
import j3d.IAppLogic;
import j3d.Window;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import util.Resource;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLEngine extends Engine implements Resource {
    private final IAppLogic appLogic;

    public OpenGLEngine(String windowTitle, IAppLogic appLogic) {
        super(windowTitle, OpenGLWindow::new);
        this.appLogic = appLogic;

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);

        appLogic.init(window);
        running = true;
    }

    public static void initializeHeadless() {
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

        long windowHandle = glfwCreateWindow(1, 1, "Headless OpenGL", NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetErrorCallback((int errorCode, long msgPtr) ->
            System.out.printf("Error code [%s], msg [%s]%n", errorCode, MemoryUtil.memUTF8(msgPtr))
        );
        glfwMakeContextCurrent(windowHandle);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);
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
    protected void update(Window window, int elapsedTimeMillis) {
        appLogic.update(window, elapsedTimeMillis);
    }

    @Override
    protected void input(Window window, int elapsedTimeMillis) {
        appLogic.input(window, elapsedTimeMillis);
    }
}
