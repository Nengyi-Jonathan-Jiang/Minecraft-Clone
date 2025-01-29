package util;

public interface SimpleAutoCloseable extends AutoCloseable {
    default void close() {}
}
