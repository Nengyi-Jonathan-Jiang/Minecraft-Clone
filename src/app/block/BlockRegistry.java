package app.block;

import app.block.model.BlockModel;
import org.joml.Vector2f;
import org.joml.Vector2i;
import util.DenseIntMap;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private BlockRegistry() {}

    private static final Map<String, BlockModel> models = new HashMap<>();
    private static final DenseIntMap<Block> blocks = new DenseIntMap<>();
    private static final Map<String, Integer> blockIDs = new HashMap<>();

    public static void registerBlock(Block block) {
        blocks.put(block.getID(), block);
        blockIDs.put(block.getName(), block.getID());
    }

    public static void registerModel(String name, BlockModel model) {
        models.put(name, model);
    }

    public static BlockModel getModel(String name) {
        return models.get(name);
    }

    public static Block getBlock(int id){
        return blocks.get(id);
    }

    public static Block getBlock(String name){
        return getBlock(getBlockID(name));
    }

    public static int getBlockID(String name) { return blockIDs.get(name); }
}
