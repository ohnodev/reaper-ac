package ac.reaper.reaperac.utils.common;


import ac.grim.reaperac.api.config.ConfigManager;

public interface ConfigReloadObserver {

    void onReload(ConfigManager config);

}
