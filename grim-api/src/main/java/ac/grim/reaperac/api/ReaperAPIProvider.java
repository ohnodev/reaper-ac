package ac.grim.reaperac.api;

import java.util.concurrent.CompletableFuture;

public final class ReaperAPIProvider {
    private static GrimAbstractAPI instance;
    private static final CompletableFuture<GrimAbstractAPI> futureInstance = new CompletableFuture<>();

    private ReaperAPIProvider() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initializes the ReaperAPI instance during mod loading.
     * This method should only be called once by the mod initializer.
     *
     * @param api The GrimAbstractAPI instance to initialize.
     * @throws IllegalStateException If the API is already initialized.
     */
    public static void init(GrimAbstractAPI api) {
        if (instance != null || futureInstance.isDone()) {
            throw new IllegalStateException("ReaperAPI is already initialized");
        }
        instance = api;
        futureInstance.complete(api); // Complete the future with the API instance
    }

    /**
     * Gets the ReaperAPI instance synchronously.
     *
     * @return The GrimAbstractAPI instance.
     * @throws IllegalStateException If the API is not loaded.
     */
    public static GrimAbstractAPI get() {
        if (instance == null) {
            throw new IllegalStateException("ReaperAPI is not loaded. Ensure the Grim mod is installed and initialized.");
        }
        return instance;
    }

    /**
     * Gets the ReaperAPI instance asynchronously.
     * The returned CompletableFuture will complete when the ReaperAPI instance is available.
     * If the API is already loaded, the future will complete immediately.
     * If the API fails to load (e.g., the mod is not installed), the future will complete exceptionally.
     *
     * @return A CompletableFuture that completes with the GrimAbstractAPI instance.
     */
    public static CompletableFuture<GrimAbstractAPI> getAsync() {
        if (instance != null) {
            // If the instance is already loaded, return a completed future
            return CompletableFuture.completedFuture(instance);
        }
        return futureInstance;
    }
}