import app.app.App;
import j3d.opengl.OpenGLEngine;

public class Main {
    public static void main(String[] args) {
        OpenGLEngine.mainThreadID = Thread.currentThread().threadId();
        new OpenGLEngine("Clone", App::new).start();
    }
}