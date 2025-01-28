package app.atlas;

import j3d.graph.Texture;

public class TextureAtlas {
    private static TextureAtlas instance;
    private static Texture texture = null;

    private TextureAtlas() {
    }

    public static Texture get() {
        return texture;
    }

    public static void setTexture(Texture texture) {
        TextureAtlas.texture = texture;
    }

    public static float scaleFactorX() {
        if(texture == null) {
            return 1.0f;
        }
        return 16f / texture.width();
    }

    public static float scaleFactorY() {
        if(texture == null) {
            return 1.0f;
        }
        return 16f / texture.height();
    }
}
