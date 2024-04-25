package j3d.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static j3d.vulkan.VulkanUtils.vkCheck;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.VK_API_VERSION_1_1;

public class VulkanInstance {

    public static final int MESSAGE_SEVERITY_BITMASK = VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
    public static final int MESSAGE_TYPE_BITMASK = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
    private static final String PORTABILITY_EXTENSION = "VK_KHR_portability_enumeration";

    private final VkInstance vkInstance;

    private VkDebugUtilsMessengerCreateInfoEXT debugUtils;
    private long vkDebugHandle;

    public VkInstance getVkInstance() {
        return vkInstance;
    }

    public VulkanInstance() {
        this(true);
    }

    public VulkanInstance(boolean useValidationLayers) {
        System.out.println("Creating Vulkan instance");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer appShortName = stack.UTF8("Clone");
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(appShortName)
                    .applicationVersion(1)
                    .pEngineName(appShortName)
                    .engineVersion(0)
                    .apiVersion(VK_API_VERSION_1_1);

            if (useValidationLayers) {
                System.out.println("Checking validation layers");
            }
            List<String> validationLayers = getSupportedValidationLayers();
            int numValidationLayers = validationLayers.size();
            boolean supportsValidation = useValidationLayers;
            if (useValidationLayers && numValidationLayers == 0) {
                supportsValidation = false;
                System.out.println("Warning: requested validation but no supported validation layers found. Falling back to no validation");
            }
            System.out.printf("Validation layers: %s%n", supportsValidation ? "on" : "off");

            // Set required  layers
            PointerBuffer requiredLayers = null;
            if (supportsValidation) {
                requiredLayers = stack.mallocPointer(numValidationLayers);
                for (int i = 0; i < numValidationLayers; i++) {
                    System.out.printf("Using validation layer [%s]%n", validationLayers.get(i));
                    requiredLayers.put(i, stack.ASCII(validationLayers.get(i)));
                }
            }

            Set<String> instanceExtensions = getInstanceExtensions();

            // GLFW Extension
            PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
            if (glfwExtensions == null) {
                throw new RuntimeException("Failed to find the GLFW platform surface extensions");
            }

            PointerBuffer requiredExtensions;

            boolean usePortability = instanceExtensions.contains(PORTABILITY_EXTENSION) && VulkanUtils.getOS() == VulkanUtils.OSType.MACOS;
            if (supportsValidation) {
                ByteBuffer vkDebugUtilsExtension = stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
                int numExtensions = usePortability ? glfwExtensions.remaining() + 2 : glfwExtensions.remaining() + 1;
                requiredExtensions = stack.mallocPointer(numExtensions);
                requiredExtensions.put(glfwExtensions).put(vkDebugUtilsExtension);
                if (usePortability) {
                    requiredExtensions.put(stack.UTF8(PORTABILITY_EXTENSION));
                }
            } else {
                int numExtensions = usePortability ? glfwExtensions.remaining() + 1 : glfwExtensions.remaining();
                requiredExtensions = stack.mallocPointer(numExtensions);
                requiredExtensions.put(glfwExtensions);
                if (usePortability) {
                    requiredExtensions.put(stack.UTF8(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
                }
            }
            requiredExtensions.flip();


            long extension = MemoryUtil.NULL;
            if (supportsValidation) {
                debugUtils = createDebugCallBack();
                extension = debugUtils.address();
            }

            // Create instance info
            @SuppressWarnings("DataFlowIssue")
            VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(extension)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(requiredLayers)
                    .ppEnabledExtensionNames(requiredExtensions);
            if (usePortability) {
                instanceInfo.flags(0x00000001); // VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR
            }

            PointerBuffer pInstance = stack.mallocPointer(1);
            vkCheck(vkCreateInstance(instanceInfo, null, pInstance), "Error creating instance");
            vkInstance = new VkInstance(pInstance.get(0), instanceInfo);

            vkDebugHandle = VK_NULL_HANDLE;
            if (supportsValidation) {
                LongBuffer longBuff = stack.mallocLong(1);
                vkCheck(vkCreateDebugUtilsMessengerEXT(vkInstance, debugUtils, null, longBuff), "Error creating debug utils");
                vkDebugHandle = longBuff.get(0);
            }
        }
    }

    public void cleanup() {
        System.out.println("Destroying Vulkan instance");
        if (vkDebugHandle != VK_NULL_HANDLE) {
            vkDestroyDebugUtilsMessengerEXT(vkInstance, vkDebugHandle, null);
        }
        vkDestroyInstance(vkInstance, null);
        if (debugUtils != null) {
            debugUtils.pfnUserCallback().free();
            debugUtils.free();
        }
    }

    private List<String> getSupportedValidationLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numLayersArr = stack.callocInt(1);
            vkEnumerateInstanceLayerProperties(numLayersArr, null);
            int numLayers = numLayersArr.get(0);

            VkLayerProperties.Buffer propsBuf = VkLayerProperties.calloc(numLayers, stack);
            vkEnumerateInstanceLayerProperties(numLayersArr, propsBuf);
            List<String> supportedLayers = new ArrayList<>();
            for (int i = 0; i < numLayers; i++) {
                VkLayerProperties props = propsBuf.get(i);
                String layerName = props.layerNameString();
                supportedLayers.add(layerName);
            }
            System.out.printf("Supported layers: %s%n", supportedLayers);

            List<String> layersToUse = new ArrayList<>();

            // Main validation layer
            if (supportedLayers.contains("VK_LAYER_KHRONOS_validation")) {
                layersToUse.add("VK_LAYER_KHRONOS_validation");
                return layersToUse;
            }

            // Fallback 1
            if (supportedLayers.contains("VK_LAYER_LUNARG_standard_validation")) {
                layersToUse.add("VK_LAYER_LUNARG_standard_validation");
                return layersToUse;
            }

            // Fallback 2 (set)
            List<String> requestedLayers = new ArrayList<>();
            requestedLayers.add("VK_LAYER_GOOGLE_threading");
            requestedLayers.add("VK_LAYER_LUNARG_parameter_validation");
            requestedLayers.add("VK_LAYER_LUNARG_object_tracker");
            requestedLayers.add("VK_LAYER_LUNARG_core_validation");
            requestedLayers.add("VK_LAYER_GOOGLE_unique_objects");

            return requestedLayers.stream().filter(supportedLayers::contains).toList();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private Set<String> getInstanceExtensions() {
        Set<String> instanceExtensions = new HashSet<>();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);

            vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);
            int numExtensions = numExtensionsBuf.get(0);

            VkExtensionProperties.Buffer instanceExtensionsProps = VkExtensionProperties.calloc(numExtensions, stack);
            vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, instanceExtensionsProps);
            for (int i = 0; i < numExtensions; i++) {
                VkExtensionProperties props = instanceExtensionsProps.get(i);
                String extensionName = props.extensionNameString();
                instanceExtensions.add(extensionName);
            }

            System.out.printf("Supported instance extensions (%s): %s%n", numExtensions, instanceExtensions);
        }
        return instanceExtensions;
    }

    @SuppressWarnings("resource")
    private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallBack() {
        return VkDebugUtilsMessengerCreateInfoEXT.calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(MESSAGE_SEVERITY_BITMASK)
                .messageType(MESSAGE_TYPE_BITMASK)
                .pfnUserCallback((messageSeverity, messageTypes, pCallbackData, pUserData) -> {
                    VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
                    if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) != 0) {
                        System.out.printf("Info: %s%n", callbackData.pMessageString());
                    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) != 0) {
                        System.out.printf("Warning: %s%n", callbackData.pMessageString());
                    } else if ((messageSeverity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0) {
                        System.out.printf("Error: %s%n", callbackData.pMessageString());
                    } else {
                        System.out.printf("Debug: %s%n", callbackData.pMessageString());
                    }
                    return VK_FALSE;
                });
    }
}
