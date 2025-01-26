package util;

public final class SharedResource<T extends Resource> implements Resource {
    private final T resource;
    private int refCount;

    public SharedResource(T resource) {
        this.resource = resource;
        this.refCount = 1;
    }

    public T get() {
        return resource;
    }

    public SharedResource<T> share() {
        refCount++;
        return this;
    }

    public void freeResources() {
        refCount--;
        if (refCount == 0) {
            resource.freeResources();
        }
    }
}
