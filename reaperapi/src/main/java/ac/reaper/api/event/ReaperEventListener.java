package ac.reaper.api.event;

@FunctionalInterface
public interface ReaperEventListener<T extends ReaperEvent> {
    void handle(T event) throws Exception;
}