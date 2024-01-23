package j3d;

import j3d.graph.Render;
import j3d.scene.Scene;

public interface IAppLogic {
    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene, long deltaTime);

    void update(Window window, Scene scene, long deltaTime);
}