package j3d.vulkan.device;

import j3d.vulkan.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.*;
import java.util.*;

import static j3d.vulkan.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK11.*;

public class LogicalDevice {

    private final PhysicalDevice physicalDevice;
    private final VkDevice vkDevice;

    public LogicalDevice(PhysicalDevice physicalDevice) {
        System.out.println("Creating logical device");

        this.physicalDevice = physicalDevice;
        try (MemoryStack stack = MemoryStack.stackPush()) {

            // Define required extensions
            Set<String> deviceExtensions = getDeviceExtensions();
            boolean usePortability = deviceExtensions.contains(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME) && VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;
            int numExtensions = usePortability ? 2 : 1;
            PointerBuffer requiredExtensions = stack.mallocPointer(numExtensions);
            requiredExtensions.put(stack.ASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
            if (usePortability) {
                requiredExtensions.put(stack.ASCII(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
            }
            requiredExtensions.flip();

            // Set up required features
            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);

            // Enable all the queue families
            VkQueueFamilyProperties.Buffer queuePropsBuff = physicalDevice.getVkQueueFamilyProps();
            int numQueuesFamilies = queuePropsBuff.capacity();
            VkDeviceQueueCreateInfo.Buffer queueCreationInfoBuf = VkDeviceQueueCreateInfo.calloc(numQueuesFamilies, stack);
            for (int i = 0; i < numQueuesFamilies; i++) {
                FloatBuffer priorities = stack.callocFloat(queuePropsBuff.get(i).queueCount());
                queueCreationInfoBuf.get(i)
                        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .queueFamilyIndex(i)
                        .pQueuePriorities(priorities);
            }

            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .ppEnabledExtensionNames(requiredExtensions)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueCreationInfoBuf);

            PointerBuffer pp = stack.mallocPointer(1);
            vkCheck(vkCreateDevice(physicalDevice.getVkPhysicalDevice(), deviceCreateInfo, null, pp),
                    "Failed to create device");
            vkDevice = new VkDevice(pp.get(0), physicalDevice.getVkPhysicalDevice(), deviceCreateInfo);
        }
    }

    public void cleanup() {
        System.out.println("Destroying Vulkan device");
        vkDestroyDevice(vkDevice, null);
    }

    private Set<String> getDeviceExtensions() {
        Set<String> deviceExtensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);
            vkEnumerateDeviceExtensionProperties(physicalDevice.getVkPhysicalDevice(), (String) null, numExtensionsBuf, null);
            int numExtensions = numExtensionsBuf.get(0);

            VkExtensionProperties.Buffer propsBuff = VkExtensionProperties.calloc(numExtensions, stack);
            vkEnumerateDeviceExtensionProperties(physicalDevice.getVkPhysicalDevice(), (String) null, numExtensionsBuf, propsBuff);
            for (int i = 0; i < numExtensions; i++) {
                VkExtensionProperties props = propsBuff.get(i);
                String extensionName = props.extensionNameString();
                deviceExtensions.add(extensionName);
            }
        }
        return deviceExtensions;
    }

    public PhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    public VkDevice getVkDevice() {
        return vkDevice;
    }

    public void waitIdle() {
        vkDeviceWaitIdle(vkDevice);
    }
}