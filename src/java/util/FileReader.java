package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility class to load fonts
 */
public final class FileReader {
    /**
     * Reads a file as a string
     * @param filename The location of the file
     */
    public static String readAsString(String filename) {
        try (var stream = readAsStream(filename)) {
            return new String(stream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static ByteBuffer readAsByteBuffer(String filename) {
        try(var stream = readAsStream(filename)) {
            byte[] bytes = stream.readAllBytes();
            ByteBuffer res = ByteBuffer.allocateDirect(bytes.length);
            res.put(bytes);
            res.flip();
            return res;
        }
        catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }


    /**
     * Reads a file as a stream
     * @param filename The location of the file
     */
    public static InputStream readAsStream(String filename) {
        var stream = FileReader.class.getResourceAsStream("/" + filename);
        if(stream == null) {
            throw new RuntimeException("Could not read file \"" + filename + "\"");
        }
        return stream;
    }
}