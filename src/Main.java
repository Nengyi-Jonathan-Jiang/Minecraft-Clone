import app.App;
import org.joml.Vector2f;
import j3d.*;
import j3d.graph.*;
import j3d.scene.*;

import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    public static void main(String[] args) {
        new Engine("Clone", new App()).start();
    }
}
