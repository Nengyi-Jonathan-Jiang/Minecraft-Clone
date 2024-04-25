package j3d.graph;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class UniformsMap {

    private final int programId;
    private final Map<String, Integer> uniforms;

    public UniformsMap(int programId) {
        this.programId = programId;
        uniforms = new HashMap<>();
    }

    private int getUniformLocation(String uniformName) {
        if(!uniforms.containsKey(uniformName)) {
            int uniformLocation = glGetUniformLocation(programId, uniformName);
            if (uniformLocation < 0) {
                throw new RuntimeException("Could not find uniform [" + uniformName + "] in shader program [" +
                        programId + "]");
            }
            uniforms.put(uniformName, uniformLocation);
        }

        Integer location = uniforms.get(uniformName);
        if (location == null) {
            throw new RuntimeException("Could not find uniform [" + uniformName + "]");
        }
        return location;
    }

    public void setUniform(String uniformName, int value) {
        try {
            glUniform1i(getUniformLocation(uniformName), value);
        }
        catch (Exception ignored) {}
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
        }
        catch (Exception ignored) {}
    }
}
