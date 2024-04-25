package j3d.vulkan.queue;

import j3d.vulkan.device.LogicalDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK11.*;

public class VulkanQueue {

    private final VkQueue vkQueue;

    public VulkanQueue(LogicalDevice logicalDevice, int queueFamilyIndex, int queueIndex) {
        System.out.println("Creating vulkan queue");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            vkGetDeviceQueue(logicalDevice.getVkDevice(), queueFamilyIndex, queueIndex, pQueue);
            long queue = pQueue.get(0);
            vkQueue = new VkQueue(queue, logicalDevice.getVkDevice());
        }
    }

    public VkQueue getVkQueue() {
        return vkQueue;
    }

    public void waitIdle() {
        vkQueueWaitIdle(vkQueue);
    }

}