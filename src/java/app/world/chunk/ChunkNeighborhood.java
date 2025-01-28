package app.world.chunk;

import app.world.util.ChunkOffset;
import app.world.util.WorldPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class ChunkNeighborhood implements Collection<Chunk> {
    private final Chunk xz, x_, xZ, _z, center, _Z, Xz, X_, XZ;
    private final Chunk[] chunks;

    public ChunkNeighborhood(Chunk xz, Chunk x_, Chunk xZ, Chunk _z, Chunk center, Chunk _Z, Chunk Xz, Chunk X_, Chunk XZ) {
        this.xz = xz;
        this.x_ = x_;
        this.xZ = xZ;
        this._z = _z;
        this.center = center;
        this._Z = _Z;
        this.Xz = Xz;
        this.X_ = X_;
        this.XZ = XZ;
        this.chunks = Stream.of(xz, x_, xZ, _z, center, _Z, Xz, X_, XZ).filter(Objects::nonNull).toArray(Chunk[]::new);
    }

    public boolean contains(WorldPosition position) {
        return getChunkFor(position) != null;
    }

    public Chunk getChunkFor(WorldPosition position) {
        WorldPosition relativePosition = position.sub(center.getChunkOffset(), new WorldPosition());
        ChunkOffset relativeChunkOffset = relativePosition.getChunkOffset();
        int x_diff = relativeChunkOffset.x();
        int z_diff = relativeChunkOffset.z();
        if (!Chunk.isYInRange(position.y())) return null;
        if (16 < x_diff || x_diff < -16 || 16 < z_diff || z_diff < -16) return null;

        return switch (x_diff) {
            case -16 -> switch (z_diff) {
                case -16 -> xz;
                case 0 -> x_;
                case 16 -> xZ;
                default -> throw new RuntimeException("Something bad happened");
            };
            case 0 -> switch (z_diff) {
                case -16 -> _z;
                case 0 -> center;
                case 16 -> _Z;
                default -> throw new RuntimeException("Something bad happened");
            };
            case 16 -> switch (z_diff) {
                case -16 -> Xz;
                case 0 -> X_;
                case 16 -> XZ;
                default -> throw new RuntimeException("Something bad happened");
            };
            default -> throw new RuntimeException("Something bad happened");
        };
    }

    @Override
    public int size() {
        return this.chunks.length;
    }

    @Override
    public boolean isEmpty() {
        return this.chunks.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (Chunk chunk : this.chunks) {
            if (o == chunk) return true;
        }
        return false;
    }

    @Override
    public @NotNull Iterator<Chunk> iterator() {
        return Arrays.stream(chunks).iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T @NotNull [] toArray(@NotNull T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Chunk chunk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Chunk> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
