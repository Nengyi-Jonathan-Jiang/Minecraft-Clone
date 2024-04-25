package j3d.vulkan;


import j3d.Engine;
import j3d.IAppLogic;
import j3d.OpenGLWindow;
import j3d.Window;
import j3d.vulkan.device.LogicalDevice;
import j3d.vulkan.device.PhysicalDevice;
import j3d.vulkan.device.Surface;
import j3d.vulkan.queue.GraphicsQueue;

public class VulkanEngine extends Engine {
    private final IAppLogic appLogic;
    private final VulkanInstance vulkanInstance;

    private final LogicalDevice logicalDevice;
    private final GraphicsQueue graphicsQueue;

    private final PhysicalDevice physicalDevice;
    private final Surface surface;

    public VulkanEngine(String windowTitle, IAppLogic appLogic) {
        super(windowTitle);
        this.appLogic = appLogic;

        vulkanInstance = new VulkanInstance();
        physicalDevice = PhysicalDevice.createPhysicalDevice(vulkanInstance, "GPU 0");
        logicalDevice = new LogicalDevice(physicalDevice);
        surface = new Surface(physicalDevice, window.getWindowHandle());
        graphicsQueue = new GraphicsQueue(logicalDevice, 0);

        appLogic.init(window);
        running = true;
    }

    @Override
    public void cleanup() {
        appLogic.cleanup();

        surface.cleanup();
        logicalDevice.cleanup();
        physicalDevice.cleanup();
        vulkanInstance.cleanup();

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
