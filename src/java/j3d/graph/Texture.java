package j3d.graph;

import org.lwjgl.system.MemoryStack;
import util.FileReader;
import util.Resource;
import util.SharedResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.stb.STBImage.*;

@SuppressWarnings("unused")
public class Texture implements Resource {
    private static final Map<String, SharedResource<TextureResource>> cachedTextures = new HashMap<>();
    private final SharedResource<TextureResource> textureResource;

    /**
     * A texture consisting of a single opaque black pixel
     */
    public static final Texture DEFAULT_TEXTURE = new Texture(
        1, 1, ByteBuffer.allocateDirect(4).put(3, (byte) 255)
    );

    /**
     * Construct a texture given a size and a {@link ByteBuffer} in RGBA format.
     */
    public Texture(int width, int height, ByteBuffer data) {
        assert data.capacity() >= width * height * 4;
        this.textureResource = new SharedResource<>(new TextureResource(width, height, data, false));
    }

    /**
     * Equivalent to {@link Texture#Texture(String, boolean)}
     */
    public Texture(String texturePath) {
        this(texturePath, false);
    }

    /**
     * Construct a texture given a path to the image. If {@code cache} is true, the texture is cached so that subsequent
     * calls to the constructor with the same path are cheap
     */
    public Texture(String texturePath, boolean cache) {
        if (cache) {
            if (cachedTextures.containsKey(texturePath)) {
                textureResource = cachedTextures.get(texturePath).share();
            } else {
                textureResource = new SharedResource<>(new TextureResource(texturePath, true));
                cachedTextures.put(texturePath, textureResource);
            }
        } else {
            textureResource = new SharedResource<>(new TextureResource(texturePath, true));
        }
    }

    /**
     * Equivalent to {@link Texture#Texture(String, boolean)}
     */
    public static Texture getCached(String texturePath) {
        return new Texture(texturePath, true);
    }

    public void bind() {
        bind(0);
    }

    public void bind(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        this.textureResource.get().bind();
    }

    public void freeResources() {
        this.textureResource.freeResources();
    }

    public static void cleanupCachedTextures() {
        for (var texture : cachedTextures.values()) {
            texture.freeResources();
        }
    }

    public int width() {
        return this.textureResource.get().width;
    }

    public int height() {
        return this.textureResource.get().height;
    }

    private static class TextureResource implements Resource {
        int textureID;
        final String texturePath;
        final int width, height;
        final boolean isShared;

        TextureResource(int width, int height, ByteBuffer buf, boolean isShared) {
            this.texturePath = "<data from buffer>";
            this.width = width;
            this.height = height;
            this.isShared = isShared;
            generateTexture(width, height, buf);
        }

        TextureResource(String texturePath, boolean isShared) {
            this.isShared = isShared;

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

        void bind() {
            glBindTexture(GL_TEXTURE_2D, textureID);
        }

        @Override
        public void freeResources() {
            glDeleteTextures(textureID);
        }

        void generateTexture(int width, int height, ByteBuffer buf) {
            textureID = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, textureID);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glGenerateMipmap(GL_TEXTURE_2D);
        }
    }
}
