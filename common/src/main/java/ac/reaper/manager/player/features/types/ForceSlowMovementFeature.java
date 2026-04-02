package ac.reaper.manager.player.features.types;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.player.ReaperPlayer;

public class ForceSlowMovementFeature implements ReaperFeature {

    @Override
    public String getName() {
        return "ForceSlowMovement";
    }

    @Override
    public void setState(ReaperPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setForceSlowMovement(true);
            case DISABLED -> player.setForceSlowMovement(false);
            default -> player.setForceSlowMovement(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(ReaperPlayer player) {
        return player.isForceSlowMovement();
    }

    @Override
    public boolean isEnabledInConfig(ReaperPlayer player, ConfigManager config) {
        return config.getBooleanElse("force-slow-movement", true);
    }

}
