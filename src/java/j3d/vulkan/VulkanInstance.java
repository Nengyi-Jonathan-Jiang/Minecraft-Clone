package j3d.vulkan;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class VulkanInstance {
    public VulkanInstance() {
        System.out.println("Creating Vulkan instance");
        try(MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer appShortName = stack.UTF8("Clone");

        }
    }
}
