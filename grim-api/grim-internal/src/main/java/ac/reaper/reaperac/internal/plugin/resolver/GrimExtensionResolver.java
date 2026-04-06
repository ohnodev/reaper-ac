package ac.reaper.reaperac.internal.plugin.resolver;

import ac.reaper.reaperac.api.plugin.GrimPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * A functional interface responsible for attempting to resolve a generic context object
 * into a {@link GrimPlugin}.
 * <p>
 * Implementations of this are provided by the core ReaperAC platform module (e.g., for Bukkit, Fabric)
 * and registered with the central GrimExtensionManager.
 */
@FunctionalInterface
public interface GrimExtensionResolver {

    /**
     * Attempts to resolve the given context object into a GrimPlugin.
     *
     * @param context The context object to resolve (e.g., a Bukkit Plugin, a Plugin Class, a Fabric Mod).
     * @return A GrimPlugin if this resolver supports the context type, otherwise null.
     */
    @Nullable GrimPlugin resolve(@NotNull Object context);

}
