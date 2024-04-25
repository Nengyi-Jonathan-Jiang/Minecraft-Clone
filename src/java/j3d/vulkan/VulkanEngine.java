package j3d.vulkan;


import j3d.IAppLogic;
import j3d.Engine;
import j3d.Window;
import j3d.ogl.OpenGLEngine;

public class VulkanEngine extends Engine {
    private final IAppLogic appLogic;
    private final VulkanInstance vulkanInstance;

    public VulkanEngine(String windowTitle, IAppLogic appLogic) {
        super(windowTitle);
        this.appLogic = appLogic;

        vulkanInstance = new VulkanInstance();

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
    protected void input(Window window, long elapsedTime) {
        appLogic.input(window, elapsedTime);
    }
}
