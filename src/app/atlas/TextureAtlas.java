package app.atlas;

import j3d.graph.Texture;
import j3d.graph.TextureCache;

public class TextureAtlas {
    private static TextureAtlas instance;
    private TextureAtlas(){}

    private static Texture texture = new TextureCache().getTexture(TextureCache.DEFAULT_TEXTURE);

    public static Texture get() {
        return texture;
    }

    public static void setTexture(Texture texture){
        TextureAtlas.texture = texture;
    }

    public static float scaleFactorX() {
        return texture.getWidth() / 16f;
    }
    public static float scaleFactorY() {
        return texture.getHeight() / 16f;
    }
}
