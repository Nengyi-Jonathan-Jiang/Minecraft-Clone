package app.block;

import app.block.model.BlockModel;
import org.joml.Vector2i;

import java.util.List;

public class Block {
    private static int nextID = 1;
    private final int id;
    private final String name;
    private final String displayName;
    private final BlockModel model;
    private final Vector2i texOffset;
    private final List<String> tags;

    public Block(String name, String displayName, BlockModel model, Vector2i texOffset, List<String> tags) {
        this.name = name;
        this.displayName = displayName;
        this.model = model;
        this.texOffset = texOffset;
        this.tags = tags;
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

    public Vector2i getTexOffset() {
        return texOffset;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}