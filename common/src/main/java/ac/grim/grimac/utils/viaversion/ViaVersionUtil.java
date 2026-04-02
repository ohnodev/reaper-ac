package ac.grim.grimac.utils.viaversion;

import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ViaVersionUtil {
    // Reaper AC is intentionally single-protocol (Fabric 26.1): keep Via* disabled.
    public static final boolean isAvailable = false;

    static {
        if (ReflectionUtils.hasClass("us.myles.ViaVersion.api.Via")) {
            LogUtil.error("Using unsupported ViaVersion 4.0 API, update ViaVersion to 5.0");
        }
    }

    public static void injectHooks() {
        // Intentionally no-op: this fork does not load Via hooks.
    }
}
