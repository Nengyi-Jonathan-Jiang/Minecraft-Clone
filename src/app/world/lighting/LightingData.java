package app.world.lighting;

import app.world.World;
import org.joml.Vector3i;
import util.MathUtil;

import java.util.Arrays;

public class LightingData {
    private final int[][][] blockLight;
    private final Vector3i[][][] lightingSource;

    public LightingData() {
        blockLight = new int[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
        lightingSource = new Vector3i[World.CHUNK_SIZE][World.CHUNK_HEIGHT][World.CHUNK_SIZE];
    }

    public void clear() {
        for(int[][] i : blockLight) for(int[] j : i) Arrays.fill(j, 0);
        for(Vector3i[][] i : lightingSource) for(Vector3i[] j : i) Arrays.fill(j, null);
    }

    public int getBlockLightAt(Vector3i pos) {
        if(pos.y >= World.CHUNK_HEIGHT) return 15;

        int x = MathUtil.clamp(pos.x, 0, World.CHUNK_SIZE - 1);
        int y = MathUtil.clamp(pos.y, 0, World.CHUNK_HEIGHT - 1);
        int z = MathUtil.clamp(pos.z, 0, World.CHUNK_SIZE - 1);

        return blockLight[x][y][z];
    }

    public void setBlockLightAt(Vector3i pos, int level) {
        blockLight[pos.x][pos.y][pos.z] = level;
    }

    public void setBlockLightSourceAt(Vector3i pos, Vector3i source) {
        lightingSource[pos.x][pos.y][pos.z] = source;
    }
    public Vector3i getBlockLightSourceAt(Vector3i pos) {
        return lightingSource[pos.x][pos.y][pos.z];
    }
}