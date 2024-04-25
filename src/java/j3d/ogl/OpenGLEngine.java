package j3d.ogl;


import j3d.IAppLogic;
import j3d.Engine;
import j3d.OpenGLWindow;
import j3d.Window;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;

public class OpenGLEngine extends Engine {
    private final IAppLogic appLogic;

    public OpenGLEngine(String windowTitle, IAppLogic appLogic) {
        super(windowTitle);
        this.appLogic = appLogic;

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);

        appLogic.init(window);
        running = true;
    }

    @Override
    public void cleanup() {
        appLogic.cleanup();
        window.cleanup();
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
    protected void update(Window window, long elapsedTime) {
        appLogic.update(window, elapsedTime);
    }

    @Override
    protected void input(OpenGLWindow window, long elapsedTime) {
        appLogic.input(window, elapsedTime);
    }
}
