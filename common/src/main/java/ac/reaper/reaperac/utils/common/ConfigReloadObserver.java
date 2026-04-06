package ac.reaper.reaperac.utils.common;


import ac.reaper.reaperac.api.config.ConfigManager;

public interface ConfigReloadObserver {

    void onReload(ConfigManager config);

}
