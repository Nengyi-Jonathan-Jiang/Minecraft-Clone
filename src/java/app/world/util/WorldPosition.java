package app.world.util;

public class WorldPosition extends Vec3i {
    public WorldPosition(int x, int y, int z) {
        super(x, y, z);
    }

    public WorldPosition() {
        super();
    }

    @Override
    public String toString() {
        return "WorldPosition" + super.toString();
    }

    public PositionInChunk getPositionInChunk() {
        return new PositionInChunk(x(), y(), z());
    }
}
