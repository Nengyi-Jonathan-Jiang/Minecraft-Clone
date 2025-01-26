package app.block;

import app.block.model.BlockModel;
import org.joml.Vector2i;

import java.util.List;

public class Block {
    private static int nextID = 1;

    public final int id;
    public final String name;
    public final String displayName;
    public final BlockModel model;
    public final Vector2i texOffset;
    public final int opacity;
    public final List<String> tags;

    public Block(String name, String displayName, BlockModel model, Vector2i texOffset, int opacity, List<String> tags) {
        this.name = name;
        this.displayName = displayName;
        this.model = model;
        this.texOffset = texOffset;
        this.opacity = opacity;
        this.tags = tags;
        id = nextID++;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}