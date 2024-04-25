package vulkantest;

import j3d.vulkan.VulkanEngine;
import vulkantest.app.VulkanTestApp;

public class VulkanTestMain {
    public static void main(String[] args) {
        new VulkanEngine("Vulkan test", new VulkanTestApp()).start();
    }
}
