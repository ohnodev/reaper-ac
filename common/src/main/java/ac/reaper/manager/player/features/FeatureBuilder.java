package ac.reaper.manager.player.features;

import ac.reaper.manager.player.features.types.ReaperFeature;
import ac.reaper.utils.anticheat.LogUtil;
import com.google.common.collect.ImmutableMap;

import java.util.regex.Pattern;

public class FeatureBuilder {

    private static final Pattern VALID = Pattern.compile("[a-zA-Z0-9_]{1,64}");
    private final ImmutableMap.Builder<String, ReaperFeature> mapBuilder = ImmutableMap.builder();

    public <T extends ReaperFeature> void register(T feature) {
        if (!VALID.matcher(feature.getName()).matches()) {
            LogUtil.error("Invalid feature name: " + feature.getName());
            return;
        }
        mapBuilder.put(feature.getName(), feature);
    }

    public ImmutableMap<String, ReaperFeature> buildMap() {
        return mapBuilder.build();
    }

}
