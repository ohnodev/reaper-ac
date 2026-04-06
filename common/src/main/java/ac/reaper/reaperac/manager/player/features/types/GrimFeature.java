package ac.reaper.reaperac.manager.player.features.types;

import ac.grim.reaperac.api.config.ConfigManager;
import ac.grim.reaperac.api.feature.FeatureState;
import ac.reaper.reaperac.player.GrimPlayer;

public interface GrimFeature {
    String getName();

    void setState(GrimPlayer player, ConfigManager config, FeatureState state);

    boolean isEnabled(GrimPlayer player);

    boolean isEnabledInConfig(GrimPlayer player, ConfigManager config);
}
