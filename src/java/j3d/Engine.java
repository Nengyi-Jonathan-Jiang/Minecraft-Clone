package j3d;


import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;

public class Engine {
    private final IAppLogic appLogic;
    private final Window window;
    private boolean running;
    private static final int targetFps = 60;
    private static final int targetUps = 60;

    public Engine(String windowTitle, IAppLogic appLogic) {
        window = new Window(windowTitle, () -> {
            resize();
            return null;
        });
        this.appLogic = appLogic;

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);

        appLogic.init(window);
        running = true;
    }

    private void cleanup() {
        appLogic.cleanup();
        window.cleanup();
    }

    private void resize() {
        appLogic.resize(window.getWidth(), window.getHeight());
    }

    private void run() {
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
                appLogic.input(window, now - initialTime);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                appLogic.update(window, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (targetFps <= 0 || deltaFps >= 1) {
                appLogic.draw(window);
                deltaFps--;
                window.updateScreen();
            }
            initialTime = now;
        }

        cleanup();
    }

    public void start() {
        running = true;
        run();
    }

    public void stop() {
        running = false;
    }
}
