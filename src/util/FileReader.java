package util;

import java.io.IOException;

/**
 * Utility class to load fonts
 */
public final class FileReader {
    /**
     * Reads a file as a string
     * @param filename The location of the file
     */
    public static String readAsString(String filename) {
        try (var stream = FileReader.class.getResourceAsStream("/" + filename)) {
            assert stream != null;
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}