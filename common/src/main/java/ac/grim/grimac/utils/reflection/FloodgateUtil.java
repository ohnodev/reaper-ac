package ac.grim.grimac.utils.reflection;

import lombok.experimental.UtilityClass;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

@UtilityClass
public class FloodgateUtil {
    private static final boolean floodgate = ReflectionUtils.hasClass("org.geysermc.floodgate.api.FloodgateApi");

    public static boolean isFloodgatePlayer(UUID uuid) {
        return floodgate && FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }
}
