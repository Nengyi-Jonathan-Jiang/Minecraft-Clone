package app.world.lighting;

import app.block.BlockRegistry;
import app.block.model.BlockModel;
import app.world.chunk.ChunkNeighborhood;
import app.world.util.WorldPosition;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static util.MathUtil.addAll;

public class AOData {
    private final WorldPosition position;
    private final ChunkNeighborhood neighborhood;

    public AOData(WorldPosition position, ChunkNeighborhood neighborhood) {
        this.position = position;
        this.neighborhood = neighborhood;
    }

    private int getLightAtIfLoadedOrDefault(WorldPosition p, int defaultValue) {
        return neighborhood.contains(p)
            ? neighborhood.getChunkFor(p).getLightingData().getBlockLightAt(p.getPositionInChunk())
            : defaultValue;
    }

    private int getOpacityIfLoadedOrDefault(WorldPosition p, int defaultValue) {
        return neighborhood.contains(p)
            ? BlockRegistry.getBlock(neighborhood.getChunkFor(p).getBlockIDAt(p.getPositionInChunk())).opacity
            : defaultValue;
    }

    public Vector4f getAOForPoint(BlockModel.FaceDirection faceDirection) {
        // The block adjacent to this face.
        WorldPosition adjacentPos = position.add(faceDirection.direction, new WorldPosition());

        if (!neighborhood.contains(adjacentPos))
            return new Vector4f(0);

        int baseBlockLight = neighborhood.getChunkFor(adjacentPos).getLightingData().getBlockLightAt(adjacentPos.getPositionInChunk());

        float lightMultiplier = faceDirection.lightMultiplier;

        // Light levels for corners
        int l_xy = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.t1, faceDirection.t2), baseBlockLight);
        int l_Xy = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.T1, faceDirection.t2), baseBlockLight);
        int l_xY = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.t1, faceDirection.T2), baseBlockLight);
        int l_XY = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.T1, faceDirection.T2), baseBlockLight);

        // Light levels for edges
        int l_x = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.t1), baseBlockLight);
        int l_y = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.t2), baseBlockLight);
        int l_X = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.T1), baseBlockLight);
        int l_Y = getLightAtIfLoadedOrDefault(addAll(new WorldPosition(), adjacentPos, faceDirection.T2), baseBlockLight);

        return new Vector4f(
            calculateWeightedAOValue(
                baseBlockLight, l_x, l_y, l_xy
            ),
            calculateWeightedAOValue(
                baseBlockLight, l_X, l_y, l_Xy
            ),
            calculateWeightedAOValue(
                baseBlockLight, l_x, l_Y, l_xY
            ),
            calculateWeightedAOValue(
                baseBlockLight, l_X, l_Y, l_XY
            )
        ).mul(lightMultiplier / 15f);
    }

    public Vector2f getInterpolatorForPoint(Vector3f point, BlockModel.FaceDirection faceDirection) {
        // Get rid of the component in the direction of the face normal
        var mask = faceDirection.direction.toVector3f().absolute().sub(1, 1, 1).absolute();

        var flattenedPoint = mask.mul(point);

        float x = flattenedPoint.dot(faceDirection.T1.toVector3f());
        float y = flattenedPoint.dot(faceDirection.T2.toVector3f());

        return new Vector2f(x, y);
    }

    private float calculateWeightedAOValue(
        float current,
        float side1,
        float side2,
        float corner
    ) {
        // Use quadratic mean for more shadow
        return (float) Math.sqrt(
            current * current + side1 * side1 + side2 * side2 + corner * corner
        ) / 2;
    }
}
