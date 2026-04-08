package ac.reaper.reaperac.utils.data.tags;

import ac.reaper.reaperac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTags;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class SyncedTag<T> {

    private final ResourceLocation location;
    private final Set<T> values;
    private final Function<Integer, T> remapper;
    private final boolean supported;
    private static final Set<String> LOGGED_MISSING_IDS = ConcurrentHashMap.newKeySet();

    private SyncedTag(ResourceLocation location, Function<Integer, T> remapper, Set<T> defaultValues, boolean supported) {
        this.location = location;
        this.supported = supported;
        this.values = Collections.newSetFromMap(new IdentityHashMap<>());
        this.remapper = remapper;
        this.values.addAll(defaultValues);
    }

    public static <T> Builder<T> builder(ResourceLocation location) {
        return new Builder<>(location);
    }

    public ResourceLocation location() {
        return location;
    }

    public boolean contains(T value) {
        return values.contains(value);
    }

    public void readTagValues(WrapperPlayServerTags.Tag tag) {
        if (!supported) return;

        final Set<T> nextValues = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int id : tag.getValues()) {
            T mapped = remapper.apply(id);
            if (mapped != null) {
                nextValues.add(mapped);
                continue;
            }
            String dedupe = location + "#" + id;
            if (LOGGED_MISSING_IDS.add(dedupe)) {
                LogUtil.error("[CRITICAL] Missing tag remap id=" + id + " for tag=" + location
                        + " (26.2-only runtime; no legacy fallback)");
            }
        }
        if (!nextValues.isEmpty()) {
            // Server is sending tag replacement. Replace only if we decoded at least one value.
            values.clear();
            values.addAll(nextValues);
        }
    }

    public static final class Builder<T> {
        private final ResourceLocation location;
        private Function<Integer, T> remapper;
        private Set<T> defaultValues;
        private boolean supported = true;

        private Builder(ResourceLocation location) {
            this.location = location;
        }

        public Builder<T> remapper(Function<Integer, T> remapper) {
            this.remapper = remapper;
            return this;
        }

        public Builder<T> supported(boolean supported) {
            this.supported = supported;
            return this;
        }

        public Builder<T> defaults(Set<T> defaultValues) {
            this.defaultValues = defaultValues;
            return this;
        }

        public SyncedTag<T> build() {
            return new SyncedTag<>(location, remapper, defaultValues, supported);
        }
    }
}
