package app.block;

import app.block.model.BlockModel;

public class Block {
    private static int nextID = 1;
    private final int id;
    private final String name;
    private final String displayName;
    private final BlockModel model;

    public Block(String name, String displayName, BlockModel model) {
        this.name = name;
        this.displayName = displayName;
        this.model = model;
        id = nextID++;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BlockModel getModel() {
        return model;
    }
}
