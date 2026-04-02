package ac.reaper.manager.player.features.types;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.player.ReaperPlayer;

public class ExemptElytraFeature implements ReaperFeature {

    @Override
    public String getName() {
        return "ExemptElytra";
    }

    @Override
    public void setState(ReaperPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setExemptElytra(true);
            case DISABLED -> player.setExemptElytra(false);
            default -> player.setExemptElytra(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(ReaperPlayer player) {
        return player.isExemptElytra();
    }

    @Override
    public boolean isEnabledInConfig(ReaperPlayer player, ConfigManager config) {
        return config.getBooleanElse("exempt-elytra", false);
    }

}
