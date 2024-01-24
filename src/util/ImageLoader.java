package util;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Utility class to load images from files
 */
public final class ImageLoader {
    private final static Map<String, Image> images = new TreeMap<>();

    private ImageLoader() {

    }

    /**
     * Gets an image by filename. Images are cached, so no image is loaded twice
     */
    public static Image get(String filename) {
        if (images.containsKey(filename)) return images.get(filename);
        else return load(filename);
    }

    /**
     * Loads a file into the class and returns it
     * @param filename The location of the image file
     */
    private static Image load(String filename) {
        try (InputStream inputStream = FileReader.readAsStream(filename)) {
            Image image = ImageIO.read(inputStream);
            images.put(filename, image);
            System.out.println("Loaded image \"" + filename + "\"");
            return image;
        } catch (Exception e) {
            System.out.println("Could not load image \"" + filename + "\"");
            e.printStackTrace();
        }
        return null;
    }
}