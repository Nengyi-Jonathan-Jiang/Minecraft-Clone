package j3d.graph;

import app.world.util.IVec3i;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderData {

    private final int programID;
    private final Map<String, Integer> uniforms;

    public ShaderData(int programID) {
        this.programID = programID;
        uniforms = new HashMap<>();
    }

    private int getUniformLocation(String uniformName) {
        if (!uniforms.containsKey(uniformName)) {
            int uniformLocation = glGetUniformLocation(programID, uniformName);
            if (uniformLocation < 0) {
                throw new RuntimeException("Could not find uniform [" + uniformName + "] in shader program [" +
                    programID + "]");
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
        } catch (Exception ignored) {}
    }

    public void setUniform(String uniformName, IVec3i value) {
        try {
            glUniform3i(getUniformLocation(uniformName), value.x(), value.y(), value.z());
        } catch (Exception ignored) {}
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
        } catch (Exception ignored) {}
    }
}
