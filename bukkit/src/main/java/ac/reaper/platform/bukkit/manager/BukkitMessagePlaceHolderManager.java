package ac.reaper.platform.bukkit.manager;

import ac.reaper.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.platform.api.player.PlatformPlayer;
import ac.reaper.platform.bukkit.player.BukkitPlatformPlayer;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitMessagePlaceHolderManager implements MessagePlaceHolderManager {
    public static final boolean hasPlaceholderAPI = Reflection.getClassByNameWithoutException("me.clip.placeholderapi.PlaceholderAPI") != null;

    @Override
    public @NotNull String replacePlaceholders(@Nullable PlatformPlayer player, @NotNull String string) {
        if (!hasPlaceholderAPI) return string;
        return PlaceholderAPI.setPlaceholders(player instanceof BukkitPlatformPlayer bukkitPlatformPlayer ? bukkitPlatformPlayer.getBukkitPlayer() : null, string);
    }
}
