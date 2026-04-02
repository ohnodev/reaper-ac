package ac.reaper.manager.player.features.types;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.player.ReaperPlayer;

public class ForceStuckSpeedFeature implements ReaperFeature {

    @Override
    public String getName() {
        return "ForceStuckSpeed";
    }

    @Override
    public void setState(ReaperPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setForceStuckSpeed(true);
            case DISABLED -> player.setForceStuckSpeed(false);
            default -> player.setForceStuckSpeed(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(ReaperPlayer player) {
        return player.isForceStuckSpeed();
    }

    @Override
    public boolean isEnabledInConfig(ReaperPlayer player, ConfigManager config) {
        return config.getBooleanElse("force-stuck-speed", true);
    }

}
