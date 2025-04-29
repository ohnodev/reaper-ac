package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.manager.MessagePlaceHolderManager;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.bukkit.GrimACBukkitLoaderPlugin;
import ac.grim.grimac.platform.bukkit.player.BukkitPlatformPlayer;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BukkitMessagePlaceHolderManager implements MessagePlaceHolderManager {
    public static final boolean hasPlaceholderAPI = Reflection.getClassByNameWithoutException("me.clip.placeholderapi.PlaceholderAPI") != null;

    @Override
    public @NonNull String replacePlaceholders(@Nullable Sender sender, @NonNull String string) {
        if (!hasPlaceholderAPI) return string;
        CommandSender commandSender = sender == null ? null : GrimACBukkitLoaderPlugin.LOADER.getBukkitSenderFactory().unwrap(sender);
        return PlaceholderAPI.setPlaceholders(commandSender instanceof OfflinePlayer offlinePlayer ? offlinePlayer : null, string);
    }

    @Override
    public @NonNull String replacePlaceholders(@Nullable PlatformPlayer object, @NonNull String string) {
        if (!hasPlaceholderAPI) return string;
        return PlaceholderAPI.setPlaceholders(object instanceof BukkitPlatformPlayer bukkitPlatformPlayer ? bukkitPlatformPlayer.getBukkitPlayer() : null, string);
    }
}
