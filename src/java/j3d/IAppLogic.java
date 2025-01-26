package j3d;

import util.Resource;

public interface IAppLogic extends Resource {
    void init(Window window);

    void input(Window window, int deltaTime);

    void update(Window window, int deltaTime);

    void draw(Window window);

    void resize(int width, int height);
}