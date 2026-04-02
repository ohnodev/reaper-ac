package ac.reaper.manager.player.features;

import ac.reaper.ReaperAPI;
import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.feature.FeatureManager;
import ac.reaper.api.feature.FeatureState;
import ac.reaper.manager.player.features.types.*;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.common.ConfigReloadObserver;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FeatureManagerImpl implements FeatureManager, ConfigReloadObserver {

    private static final Map<String, ReaperFeature> FEATURES;

    /// @deprecated use {@link #getFeatures()}
    @Contract(pure = true)
    @Deprecated
    public static Map<String, ReaperFeature> getFEATURES() {
        return getFeatures();
    }

    @Contract(pure = true)
    public static Map<String, ReaperFeature> getFeatures() {
        return FEATURES;
    }

    static {
        FeatureBuilder builder = new FeatureBuilder();
        builder.register(new ExperimentalChecksFeature());
        builder.register(new ExemptElytraFeature());
        builder.register(new ForceStuckSpeedFeature());
        builder.register(new ForceSlowMovementFeature());
        FEATURES = builder.buildMap();
    }

    private final Map<String, FeatureState> states = new HashMap<>();

    private final ReaperPlayer player;

    public FeatureManagerImpl(ReaperPlayer player) {
        this.player = player;
        for (ReaperFeature value : FEATURES.values()) states.put(value.getName(), FeatureState.UNSET);
    }

    @Override
    public Collection<String> getFeatureKeys() {
        return ImmutableSet.copyOf(FEATURES.keySet());
    }

    @Override
    public @Nullable FeatureState getFeatureState(String key) {
        return states.get(key);
    }

    @Override
    public boolean isFeatureEnabled(String key) {
        ReaperFeature feature = FEATURES.get(key);
        if (feature == null) return false;
        return feature.isEnabled(player);
    }

    @Override
    public boolean setFeatureState(String key, FeatureState tristate) {
        ReaperFeature feature = FEATURES.get(key);
        if (feature == null) return false;
        states.put(key, tristate);
        return true;
    }

    @Override
    public void reload() {
        onReload(ReaperAPI.INSTANCE.getExternalAPI().getConfigManager());
    }

    @Override
    public void onReload(ConfigManager config) {
        for (Map.Entry<String, FeatureState> entry : states.entrySet()) {
            String key = entry.getKey();
            FeatureState state = entry.getValue();
            ReaperFeature feature = FEATURES.get(key);
            if (feature == null) continue;
            feature.setState(player, config, state);
        }
    }

}
