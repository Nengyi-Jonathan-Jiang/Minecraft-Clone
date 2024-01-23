package app.block.model;

public record BlockModel(
        PartialMesh top, PartialMesh front, PartialMesh right,
        PartialMesh bottom, PartialMesh back, PartialMesh left,
        PartialMesh inner) {
    public static BlockModel combine(BlockModel a, BlockModel b) {
        return new BlockModel(
                PartialMesh.combine(a.top, b.top),
                PartialMesh.combine(a.front, b.front),
                PartialMesh.combine(a.right, b.right),
                PartialMesh.combine(a.bottom, b.bottom),
                PartialMesh.combine(a.back, b.back),
                PartialMesh.combine(a.left, b.left),
                PartialMesh.combine(a.inner, b.inner)
        );
    }
}