package app.block;

public class BlockBuilder {
    private String name = null;
    private String displayName = null;

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

    public Block getResult() {
        if(hasBeenBuilt) {
            throw new RuntimeException("BlockBuilder can only be used once.");
        }
        var res = new Block(name, displayName == null ? name : displayName);
        hasBeenBuilt = true;
        return res;
    }
}