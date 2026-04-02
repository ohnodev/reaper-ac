package ac.reaper.manager.player.features.types;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.player.ReaperPlayer;

public interface ReaperFeature {
    String getName();

    void setState(ReaperPlayer player, ConfigManager config, FeatureState state);

    boolean isEnabled(ReaperPlayer player);

    boolean isEnabledInConfig(ReaperPlayer player, ConfigManager config);
}
