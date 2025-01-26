package app.atlas;

import j3d.graph.Texture;

public class TextureAtlas {
    private static TextureAtlas instance;
    private static Texture texture = Texture.DEFAULT_TEXTURE;

    private TextureAtlas() {
    }

    public static Texture get() {
        return texture;
    }

    public static void setTexture(Texture texture) {
        TextureAtlas.texture = texture;
    }

    public static float scaleFactorX() {
        return 16f / texture.width();
    }

    public static float scaleFactorY() {
        return 16f / texture.height();
    }
}
