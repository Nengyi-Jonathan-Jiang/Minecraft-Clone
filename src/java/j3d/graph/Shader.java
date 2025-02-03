package j3d.graph;

import org.lwjgl.opengl.GL43;
import util.FileReader;
import util.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL43.*;

public class Shader implements Resource {

    private final int programID;
    private final ShaderData uniforms;

    public enum ShaderType {
        Vertex(GL_VERTEX_SHADER), Fragment(GL_FRAGMENT_SHADER),
        Geometry(GL_GEOMETRY_SHADER),
        Compute(GL_COMPUTE_SHADER);


        private final int glEnumValue;

        ShaderType(int glEnumValue) {
            this.glEnumValue = glEnumValue;
        }
    }

    public static Shader createBasicShader(String vertexShaderPath, String fragmentShaderPath) {
        return new Shader(
            new ShaderModuleData(vertexShaderPath, ShaderType.Vertex),
            new ShaderModuleData(fragmentShaderPath, ShaderType.Fragment)
        );
    }

    public Shader(ShaderModuleData... shaderModuleDataList) {
        programID = glCreateProgram();
        if (programID == 0) {
            throw new RuntimeException("Could not create Shader");
        }

        List<Integer> shaderIDs = new ArrayList<>();
        for (ShaderModuleData s : shaderModuleDataList) {
            String shaderCode = FileReader.readAsString(s.shaderFile);
            String[] inputLines = shaderCode.split("\n");
            ArrayList<String> outputLines = new ArrayList<>();
            for (String line : inputLines) {
                if (line.matches("#include\\s+\"[\\w/.]+\"\\s*")) {
                    String libraryName = line.split("\\s+")[1];
                    libraryName = libraryName.substring(1, libraryName.length() - 1);
                    String librarySource = FileReader.readAsString(libraryName);
                    outputLines.add(librarySource);
                }
                else {
                    outputLines.add(line);
                }
            }

            shaderCode = String.join("\n", outputLines);
            shaderIDs.add(
                createShader(shaderCode, s.shaderType.glEnumValue)
            );
        }

        link(shaderIDs);

        uniforms = new ShaderData(programID);
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void freeResources() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }

    protected int createShader(String shaderCode, int shaderType) {

        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            String[] prettyPrint = shaderCode.split("\n");
            throw new RuntimeException(
                "Error compiling Shader code: \n"
                    + glGetShaderInfoLog(shaderID, 2048)
                    + "\n\n(Full code):\n\n"
                    + IntStream.range(0, prettyPrint.length)
                        .mapToObj(i -> (i + 1) + ":   \t" + prettyPrint[i])
                        .collect(Collectors.joining("\n"))
            );
        }

        glAttachShader(programID, shaderID);

        return shaderID;
    }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(programID, 2048));
        }

        shaderModules.forEach(s -> glDetachShader(programID, s));
        shaderModules.forEach(GL43::glDeleteShader);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public record ShaderModuleData(String shaderFile, ShaderType shaderType) {}

    public record ShaderLibraryFileEntry(String file, String name) {}

    public ShaderData uniforms() {
        return uniforms;
    }
}