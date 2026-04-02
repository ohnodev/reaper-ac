package ac.reaper.manager.player.features.types;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.player.ReaperPlayer;

public class ExperimentalChecksFeature implements ReaperFeature {

    @Override
    public String getName() {
        return "ExperimentalChecks";
    }

    @Override
    public void setState(ReaperPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setExperimentalChecks(true);
            case DISABLED -> player.setExperimentalChecks(false);
            default -> player.setExperimentalChecks(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(ReaperPlayer player) {
        return player.isExperimentalChecks();
    }

    @Override
    public boolean isEnabledInConfig(ReaperPlayer player, ConfigManager config) {
        return config.getBooleanElse("experimental-checks", false);
    }

}
