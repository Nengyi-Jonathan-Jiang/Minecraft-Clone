package app.app;

import app.block.BlockBuilder;
import app.block.BlockRegistry;
import app.block.model.BlockModelLoader;
import org.joml.Vector2i;

public class DefaultBlocksInitializer {
    public static void run() {
        BlockRegistry.registerModel("cube", BlockModelLoader.loadModel("block/cube.model"));

        BlockBuilder basicBlockBuilder = new BlockBuilder().model("cube");

        BlockBuilder naturalBlockBuilder = basicBlockBuilder.withTag("natural");

        BlockBuilder oreBlockBuilder = naturalBlockBuilder.withTag("ore");

        BlockBuilder netherBlockBuilder = naturalBlockBuilder.withTag("nether");
        BlockBuilder netherOreBlockBuilder = netherBlockBuilder.withTag("ore");

        BlockBuilder woodBlockBuilder = naturalBlockBuilder.withTag("wood");

        BlockRegistry.registerBlock(naturalBlockBuilder
                .name("dirt", "Dirt Block")
                .texOffset(new Vector2i(0, 2))
                .getResult());
        BlockRegistry.registerBlock(naturalBlockBuilder
                .name("grass", "Grass Block")
                .texOffset(new Vector2i(0, 4))
                .getResult());
        BlockRegistry.registerBlock(naturalBlockBuilder
                .name("stone", "Stone Block")
                .texOffset(new Vector2i(0, 6))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("coal_ore", "Coal Ore")
                .texOffset(new Vector2i(0, 8))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("iron_ore", "Coal Ore")
                .texOffset(new Vector2i(0, 10))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("gold_ore", "Gold Ore")
                .texOffset(new Vector2i(0, 12))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("redstone_ore", "Redstone Ore")
                .texOffset(new Vector2i(0, 14))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("diamond_ore", "Diamond Ore")
                .texOffset(new Vector2i(0, 16))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("emerald_ore", "Emerald Ore")
                .texOffset(new Vector2i(0, 18))
                .getResult());
        BlockRegistry.registerBlock(oreBlockBuilder
                .name("lapis_ore", "Lapis Ore")
                .texOffset(new Vector2i(0, 20))
                .getResult());
        BlockRegistry.registerBlock(netherOreBlockBuilder
                .name("nether_quartz_ore", "Nether Quartz Ore")
                .texOffset(new Vector2i(0, 22))
                .getResult());
        BlockRegistry.registerBlock(netherOreBlockBuilder
                .name("nether_gold_ore", "Nether Gold Ore")
                .texOffset(new Vector2i(0, 24))
                .getResult());
        BlockRegistry.registerBlock(netherOreBlockBuilder
                .name("ancient_debris", "Ancient Debris")
                .texOffset(new Vector2i(0, 26))
                .getResult());
        BlockRegistry.registerBlock(netherBlockBuilder
                .name("netherrack", "Netherrack")
                .texOffset(new Vector2i(0, 28))
                .getResult());
        BlockRegistry.registerBlock(naturalBlockBuilder
                .name("bedrock", "Bedrock")
                .texOffset(new Vector2i(0, 30))
                .getResult());
    }
}
