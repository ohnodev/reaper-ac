package ac.grim.grimac.utils.reflection;

import lombok.experimental.UtilityClass;
import org.geysermc.api.Geyser;

import java.util.UUID;

@UtilityClass
public class GeyserUtil {
    private static final boolean geyser = ReflectionUtils.hasClass("org.geysermc.api.Geyser");

    public static boolean isGeyserPlayer(UUID uuid) {
        return geyser && Geyser.api().isBedrockPlayer(uuid);
    }
}
