package ac.grim.reaperac.api.event;

@FunctionalInterface
public interface GrimEventListener<T extends GrimEvent> {
    void handle(T event) throws Exception;
}