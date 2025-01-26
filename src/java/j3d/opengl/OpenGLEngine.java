package j3d.opengl;


import j3d.Engine;
import j3d.IAppLogic;
import j3d.Window;
import org.lwjgl.opengl.GL;
import util.Resource;

import static org.lwjgl.opengl.GL11.*;

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
