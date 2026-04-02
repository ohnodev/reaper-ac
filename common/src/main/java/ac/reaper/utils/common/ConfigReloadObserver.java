package ac.reaper.utils.common;


import ac.reaper.api.config.ConfigManager;

public interface ConfigReloadObserver {

    void onReload(ConfigManager config);

}
