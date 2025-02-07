package app.block;

import org.joml.Vector2i;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class BlockBuilder {
    private final String name, displayName;
    private final String model;
    private final Vector2i texOffset;
    private final List<String> tags;
    private final int opacity;

    public BlockBuilder() {
        this(null, null, null, null, 15, Collections.emptyList());
    }

    private BlockBuilder(String name, String displayName, String model, Vector2i texOffset, int opacity, List<String> tags) {
        this.name = name;
        this.displayName = displayName;
        this.model = model;
        this.texOffset = texOffset;
        this.opacity = opacity;
        this.tags = tags;
    }

    public BlockBuilder name(String name) {
        return new BlockBuilder(name, displayName, model, texOffset, opacity, tags);
    }

    public BlockBuilder name(String name, String displayName) {
        return new BlockBuilder(name, displayName, model, texOffset, opacity, tags);
    }

    public BlockBuilder displayName(String displayName) {
        return new BlockBuilder(name, displayName, model, texOffset, opacity, tags);
    }

    public BlockBuilder model(String model) {
        return new BlockBuilder(name, displayName, model, texOffset, opacity, tags);
    }

    public BlockBuilder texOffset(Vector2i texOffset) {
        return new BlockBuilder(name, displayName, model, texOffset, opacity, tags);
    }

    public BlockBuilder withTag(String tag) {
        List<String> newTags = Stream.concat(tags.stream(), Stream.of(tag)).toList();
        return new BlockBuilder(name, displayName, model, texOffset, opacity, newTags);
    }


    public Block getResult() {
        if (name == null || texOffset == null || model == null) {
            throw new RuntimeException("Block must have name, model, and texOffset");
        }

        return new Block(
            name,
            displayName == null ? name : displayName,
            BlockRegistry.getModel(model),
            texOffset,
            opacity,
            tags
        );
    }
}