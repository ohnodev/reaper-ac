package ac.reaper.reaperac.platform.api.manager;

import ac.reaper.reaperac.platform.api.PlatformPlugin;

public interface PlatformPluginManager {

    PlatformPlugin[] getPlugins();

    PlatformPlugin getPlugin(String pluginName);

    default boolean isPluginEnabled(String pluginName) {
        PlatformPlugin plugin = getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
}
