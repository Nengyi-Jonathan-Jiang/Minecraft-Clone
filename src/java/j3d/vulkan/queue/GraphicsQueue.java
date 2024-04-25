package j3d.vulkan.queue;

import j3d.vulkan.device.LogicalDevice;
import j3d.vulkan.device.PhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;

public class GraphicsQueue extends VulkanQueue {

    public GraphicsQueue(LogicalDevice logicalDevice, int queueIndex) {
        super(logicalDevice, getGraphicsQueueFamilyIndex(logicalDevice), queueIndex);
    }

    private static int getGraphicsQueueFamilyIndex(LogicalDevice logicalDevice) {
        int index = -1;
        PhysicalDevice physicalDevice = logicalDevice.getPhysicalDevice();
        VkQueueFamilyProperties.Buffer queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
        int numQueuesFamilies = queuePropsBuff.capacity();
        for (int i = 0; i < numQueuesFamilies; i++) {
            VkQueueFamilyProperties props = queuePropsBuff.get(i);
            boolean graphicsQueue = (props.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
            if (graphicsQueue) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            throw new RuntimeException("Failed to get graphics Queue family index");
        }
        return index;
    }
}
