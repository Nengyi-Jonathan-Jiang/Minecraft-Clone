package j3d.graph;

import org.lwjgl.system.MemoryStack;
import util.FileReader;

import java.nio.*;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private int textureId;
    private final String texturePath;
    private final int width, height;

    public Texture(int width, int height, ByteBuffer buf) {
        this.texturePath = "";
        this.width = width;
        this.height = height;
        generateTexture(width, height, buf);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture(String texturePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.texturePath = texturePath;
            ByteBuffer buffer = FileReader.readAsByteBuffer(texturePath);

            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer buf = stbi_load_from_memory(buffer, w, h, channels, 4);
            if (buf == null) {
                throw new RuntimeException("Image file [" + texturePath + "] not loaded: " + stbi_failure_reason());
            }

            this.width = w.get();
            this.height = h.get();

            generateTexture(width, height, buf);

            stbi_image_free(buf);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }

    private void generateTexture(int width, int height, ByteBuffer buf) {
        textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public String getTexturePath() {
        return texturePath;
    }
}
