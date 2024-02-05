package j3d;

public interface IAppLogic {
    void cleanup();

    void init(Window window);

    void input(Window window, long deltaTime);

    void update(Window window, long deltaTime);

    void draw(Window window);

    void resize(int width, int height);
}