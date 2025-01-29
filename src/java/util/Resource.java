package util;

/**
 * An interface representing an object that holds resources that must be manually freed
 */
public interface Resource {
    /**
     * Frees the resources associated with this object
     */
    void freeResources();

}
