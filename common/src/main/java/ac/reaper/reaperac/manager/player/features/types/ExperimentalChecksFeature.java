package ac.reaper.reaperac.manager.player.features.types;

import ac.grim.reaperac.api.config.ConfigManager;
import ac.grim.reaperac.api.feature.FeatureState;
import ac.reaper.reaperac.player.GrimPlayer;

public class ExperimentalChecksFeature implements GrimFeature {

    @Override
    public String getName() {
        return "ExperimentalChecks";
    }

    @Override
    public void setState(GrimPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setExperimentalChecks(true);
            case DISABLED -> player.setExperimentalChecks(false);
            default -> player.setExperimentalChecks(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(GrimPlayer player) {
        return player.isExperimentalChecks();
    }

    @Override
    public boolean isEnabledInConfig(GrimPlayer player, ConfigManager config) {
        return config.getBooleanElse("experimental-checks", false);
    }

}
