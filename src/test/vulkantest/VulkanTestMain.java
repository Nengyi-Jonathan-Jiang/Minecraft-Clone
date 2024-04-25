package vulkantest;

import j3d.ogl.OpenGLEngine;
import vulkantest.app.VulkanTestApp;

public class VulkanTestMain {
    public static void main(String[] args) {
        new OpenGLEngine("Vulkan test", new VulkanTestApp()).start();
    }
}
