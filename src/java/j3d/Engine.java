package j3d;

import j3d.ogl.OpenGLEngine;

public abstract class Engine {
    protected final OpenGLWindow window;
    protected boolean running;
    public static final int targetFps = 60;
    public static final int targetUps = 60;

    public abstract void cleanup();

    public abstract void resize();

    protected abstract void draw(Window window);

    protected abstract void update(Window window, long elapsedTime);

    protected abstract void input(OpenGLWindow window, long elapsedTime);

    protected Engine(String windowTitle) {
        window = new OpenGLWindow(windowTitle, () -> {
            resize();
            return null;
        });
    }

    public final void run() {
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / OpenGLEngine.targetUps;
        float timeR = OpenGLEngine.targetFps > 0 ? 1000.0f / OpenGLEngine.targetFps : 0;
        float deltaUpdate = 0;
        float deltaFps = 0;

        long updateTime = initialTime;
        while (running && !window.windowShouldClose()) {
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (OpenGLEngine.targetFps <= 0 || deltaFps >= 1) {
                window.getMouseInput().input();
                input(window, now - initialTime);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                update(window, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (OpenGLEngine.targetFps <= 0 || deltaFps >= 1) {
                draw(window);
                deltaFps--;
                window.updateScreen();
            }
            initialTime = now;
        }

        cleanup();
    }

    public final void start() {
        running = true;
        run();
    }

    public final void stop() {
        running = false;
    }
}
