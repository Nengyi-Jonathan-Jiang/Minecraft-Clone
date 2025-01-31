package j3d;

import util.Resource;

public abstract class Engine implements Resource {
    protected final Window window;
    protected boolean running;
    public static final int targetFps = 60;
    public static final int targetUps = 60;

    public abstract void resize();

    protected abstract void draw(Window window);

    protected abstract void update(Window window, int elapsedTimeMillis);

    protected abstract void input(Window window, int elapsedTimeMillis);

    protected Engine(String windowTitle, Window.WindowConstructor windowConstructor) {
        window = windowConstructor.call(windowTitle, this::resize);
    }

    public final void run() {
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / targetUps;
        float timeR = targetFps > 0 ? 1000.0f / targetFps : 0;
        float deltaUpdate = 0;
        float deltaFps = 0;

        long updateTime = initialTime;
        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (targetFps <= 0 || deltaFps >= 1) {
                window.getMouseInput().input();
                input(window, (int) (now - initialTime));
            }

            if (deltaUpdate >= 1) {
                int diffTimeMillis = (int) (now - updateTime);
                update(window, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFps <= 0 || deltaFps >= 1) {
                draw(window);
                deltaFps--;
                window.updateScreen();
            }
            initialTime = now;
        }

        freeResources();
    }

    public final void start() {
        running = true;
        run();
    }

    public final void stop() {
        running = false;
    }
}
