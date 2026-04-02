package ac.reaper.api.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}