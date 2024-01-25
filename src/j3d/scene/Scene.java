package j3d.scene;

import j3d.graph.*;

import java.util.*;

public class Scene {
    private final Camera camera;
    private final Projection projection;
    private final TextureCache textureCache;

    public Scene(int width, int height) {
        projection = new Projection(width, height);
        textureCache = new TextureCache();
        camera = new Camera();
    }

    public Camera getCamera() {
        return camera;
    }

    public Projection getProjection() {
        return projection;
    }

    public TextureCache getTextureCache() {
        return textureCache;
    }

    public void resize(int width, int height) {
        projection.updateProjectionMatrix(width, height);
    }
}
