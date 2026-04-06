package ac.reaper.reaperac.manager.player.features.types;

import ac.reaper.reaperac.api.config.ConfigManager;
import ac.reaper.reaperac.api.feature.FeatureState;
import ac.reaper.reaperac.player.GrimPlayer;

public class ForceSlowMovementFeature implements GrimFeature {

    @Override
    public String getName() {
        return "ForceSlowMovement";
    }

    @Override
    public void setState(GrimPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setForceSlowMovement(true);
            case DISABLED -> player.setForceSlowMovement(false);
            default -> player.setForceSlowMovement(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(GrimPlayer player) {
        return player.isForceSlowMovement();
    }

    @Override
    public boolean isEnabledInConfig(GrimPlayer player, ConfigManager config) {
        return config.getBooleanElse("force-slow-movement", true);
    }

}
