package ac.grim.reaperac.api.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}