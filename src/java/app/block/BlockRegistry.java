package app.block;

import app.block.model.BlockModel;
import util.DenseIntMap;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<String, BlockModel> models = new HashMap<>();
    private static final DenseIntMap<Block> blocks = new DenseIntMap<>();
    private static final Map<String, Integer> blockIDs = new HashMap<>();

    private BlockRegistry() {}

    public static void registerBlock(Block block) {
        blocks.put(block.id, block);
        blockIDs.put(block.name, block.id);
    }

    public static void registerModel(String name, BlockModel model) {
        models.put(name, model);
    }

    public static BlockModel getModel(String name) {
        return models.get(name);
    }

    public static Block getBlock(int id) {
        return blocks.get(id);
    }

    public static Block getBlock(String name) {
        return getBlock(getBlockID(name));
    }

    public static int getBlockID(String name) {
        return blockIDs.get(name);
    }
}
