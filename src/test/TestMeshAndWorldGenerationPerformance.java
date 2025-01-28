import app.app.DefaultBlocksInitializer;
import app.player.Player;
import app.world.World;
import app.world.chunk.Chunk;
import app.world.util.ChunkOffset;
import app.world.worldgen.WorldGenerator;
import j3d.opengl.OpenGLEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMeshAndWorldGenerationPerformance {
    @Test
    void test() {
        OpenGLEngine.initializeHeadless();

        int numChunks = 400;

        System.out.println("Registering Blocks");
        DefaultBlocksInitializer.run();

        World world = new World(new WorldGenerator(), new Player());

        System.out.println("Generating Chunks");
        for (int x = 0; x < numChunks; x++) {
            System.out.println(x + "\t/ " + numChunks);
            Chunk chunk = world.requestChunk(new ChunkOffset(x * 16, 0), true);
            Assertions.assertNotNull(chunk);
        }

        System.out.println("Building Meshes");
        for (int x = 0; x < numChunks; x++) {
            System.out.println(x + "\t/ " + numChunks);
            Chunk chunk = world.requestChunk(new ChunkOffset(x * 16, 0), true);
            chunk.getMesh();
        }
    }
}
