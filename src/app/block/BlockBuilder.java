package app.block;

import app.block.model.BlockModel;
import org.joml.Vector2i;

public class BlockBuilder {
    private String name = null;
    private String displayName = null;
    private BlockModel model;
    private Vector2i texOffset;

    private boolean hasBeenBuilt = false;


    public BlockBuilder() {}

    public BlockBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BlockBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public BlockBuilder model(BlockModel model) {
        this.model = model;
        return this;
    }

    public BlockBuilder texOffset(Vector2i texOffset) {
        this.texOffset = texOffset;
        return this;
    }

    public Block getResult() {
        if(hasBeenBuilt) {
            throw new RuntimeException("BlockBuilder can only be used once.");
        }

        if(name == null || texOffset == null || model == null) {
            throw new RuntimeException("Block must have name, model, and texOffset");
        }

        var res = new Block(name, displayName == null ? name : displayName, model, texOffset);
        hasBeenBuilt = true;
        return res;
    }
}