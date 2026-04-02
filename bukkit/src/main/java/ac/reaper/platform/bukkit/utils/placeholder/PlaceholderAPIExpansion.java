package ac.reaper.platform.bukkit.utils.placeholder;

import ac.reaper.ReaperAPI;
import ac.reaper.api.ReaperUser;
import ac.reaper.player.ReaperPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "reaper";
    }

    public @NotNull String getAuthor() {
        return String.join(", ", ReaperAPI.INSTANCE.getReaperPlugin().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return ReaperAPI.INSTANCE.getExternalAPI().getReaperVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        Set<String> staticReplacements = ReaperAPI.INSTANCE.getExternalAPI().getStaticReplacements().keySet();
        Set<String> variableReplacements = ReaperAPI.INSTANCE.getExternalAPI().getVariableReplacements().keySet();
        ArrayList<String> placeholders = new ArrayList<>(staticReplacements.size() + variableReplacements.size());
        for (String s : staticReplacements) {
            placeholders.add(s.equals("%reaper_version%") ? s : "%reaper_" + s.replaceAll("%", "") + "%");
        }
        for (String s : variableReplacements) {
            placeholders.add(s.equals("%player%") ? "%reaper_player%" : "%reaper_player_" + s.replaceAll("%", "") + "%");
        }
        return placeholders;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        for (Map.Entry<String, String> entry : ReaperAPI.INSTANCE.getExternalAPI().getStaticReplacements().entrySet()) {
            String key = entry.getKey().equals("%reaper_version%")
                    ? "version"
                    : entry.getKey().replaceAll("%", "");
            if (params.equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        if (offlinePlayer instanceof Player player) {
            ReaperPlayer reaperPlayer = ReaperAPI.INSTANCE.getPlayerDataManager().getPlayer(player.getUniqueId());
            if (reaperPlayer == null) return null;

            for (Map.Entry<String, Function<ReaperUser, String>> entry : ReaperAPI.INSTANCE.getExternalAPI().getVariableReplacements().entrySet()) {
                String key = entry.getKey().equals("%player%")
                        ? "player"
                        : "player_" + entry.getKey().replaceAll("%", "");
                if (params.equalsIgnoreCase(key)) {
                    return entry.getValue().apply(reaperPlayer);
                }
            }
        }

        return null;
    }
}
