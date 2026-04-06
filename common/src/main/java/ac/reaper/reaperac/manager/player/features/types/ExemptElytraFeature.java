package ac.reaper.reaperac.manager.player.features.types;

import ac.grim.reaperac.api.config.ConfigManager;
import ac.grim.reaperac.api.feature.FeatureState;
import ac.reaper.reaperac.player.GrimPlayer;

public class ExemptElytraFeature implements GrimFeature {

    @Override
    public String getName() {
        return "ExemptElytra";
    }

    @Override
    public void setState(GrimPlayer player, ConfigManager config, FeatureState state) {
        switch (state) {
            case ENABLED -> player.setExemptElytra(true);
            case DISABLED -> player.setExemptElytra(false);
            default -> player.setExemptElytra(isEnabledInConfig(player, config));
        }
    }

    @Override
    public boolean isEnabled(GrimPlayer player) {
        return player.isExemptElytra();
    }

    @Override
    public boolean isEnabledInConfig(GrimPlayer player, ConfigManager config) {
        return config.getBooleanElse("exempt-elytra", false);
    }

}
