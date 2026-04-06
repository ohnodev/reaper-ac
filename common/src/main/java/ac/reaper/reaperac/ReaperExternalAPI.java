package ac.reaper.reaperac;

import ac.reaper.reaperac.api.ReaperAbstractAPI;
import ac.reaper.reaperac.api.ReaperUser;
import ac.reaper.reaperac.api.alerts.AlertManager;
import ac.reaper.reaperac.api.config.ConfigManager;
import ac.reaper.reaperac.api.event.EventBus;
import ac.reaper.reaperac.api.event.events.GrimReloadEvent;
import ac.reaper.reaperac.api.plugin.ReaperPlugin;
import ac.reaper.reaperac.manager.config.ConfigManagerFileImpl;
import ac.reaper.reaperac.manager.init.start.StartableInitable;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.anticheat.MessageUtil;
import ac.reaper.reaperac.utils.common.ConfigReloadObserver;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// This is used for Reaper's external API. It has its own class just for organization.
public class ReaperExternalAPI implements ReaperAbstractAPI, ConfigReloadObserver, StartableInitable {

    private final GrimAPI api;
    @Getter
    private final Map<String, Function<ReaperUser, String>> variableReplacements = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, String> staticReplacements = new ConcurrentHashMap<>();
    private final Map<String, Function<Object, Object>> functions = new ConcurrentHashMap<>();
    private final ConfigManagerFileImpl configManagerFile = new ConfigManagerFileImpl();
    private ConfigManager configManager = null;
    private boolean started = false;

    public ReaperExternalAPI(GrimAPI api) {
        this.api = api;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return api.getEventBus();
    }

    @Override
    public @Nullable ReaperUser getReaperUser(Player player) {
        return getReaperUser(player.getUniqueId());
    }

    @Override
    public @Nullable ReaperUser getReaperUser(UUID uuid) {
        return api.getPlayerDataManager().getPlayer(uuid);
    }

    @Override
    public void registerVariable(String string, Function<ReaperUser, String> replacement) {
        variableReplacements.put(string, Objects.requireNonNull(replacement, "replacement cannot be null; use unregisterVariable(variable)"));
    }

    @Override
    public void registerVariable(String variable, String replacement) {
        staticReplacements.put(variable, Objects.requireNonNull(replacement, "replacement cannot be null; use unregisterVariable(variable)"));
    }

    @Override
    public void unregisterVariable(String variable) {
        variableReplacements.remove(variable);
        staticReplacements.remove(variable);
    }

    @Override
    public String getReaperVersion() {
        return api.getReaperPlugin().getDescription().getVersion();
    }

    @Override
    public void registerFunction(String key, Function<Object, Object> function) {
        if (function == null) {
            functions.remove(key);
        } else {
            functions.put(key, function);
        }
    }

    @Override
    public Function<Object, Object> getFunction(String key) {
        return functions.get(key);
    }

    @Override
    public AlertManager getAlertManager() {
        return api.getAlertManager();
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public boolean hasStarted() {
        return started;
    }

    @Override
    public int getCurrentTick() {
        return api.getTickManager().currentTick;
    }

    @Override
    public @NotNull ReaperPlugin getReaperPlugin(@NotNull Object o) {
        return this.api.getExtensionManager().getPlugin(o);
    }

    // on load, load the config & register the service
    public void load() {
        reload(configManagerFile);
        api.getLoader().registerAPIService();
    }

    // handles any config loading that's needed to be done after load
    @Override
    public void start() {
        started = true;
        try {
            api.getConfigManager().start();
        } catch (Exception e) {
            LogUtil.error("Failed to start config manager.", e);
        }
    }

    @Override
    public void reload(ConfigManager config) {
        if (config.isLoadedAsync() && started) {
            api.getScheduler().getAsyncScheduler().runNow(api.getReaperPlugin(),
                    () -> successfulReload(config));
        } else {
            successfulReload(config);
        }
    }

    @Override
    public CompletableFuture<Boolean> reloadAsync(ConfigManager config) {
        if (config.isLoadedAsync() && started) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            api.getScheduler().getAsyncScheduler().runNow(api.getReaperPlugin(),
                    () -> future.complete(successfulReload(config)));
            return future;
        }
        return CompletableFuture.completedFuture(successfulReload(config));
    }

    private boolean successfulReload(ConfigManager config) {
        try {
            config.reload();
            api.getConfigManager().load(config);
            if (started) api.getConfigManager().start();
            onReload(config);
            if (started)
                api.getScheduler().getAsyncScheduler().runNow(api.getReaperPlugin(),
                        () -> api.getEventBus().post(new GrimReloadEvent(true)));
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to reload config", e);
        }
        if (started)
            api.getScheduler().getAsyncScheduler().runNow(api.getReaperPlugin(),
                    () -> api.getEventBus().post(new GrimReloadEvent(false)));
        return false;
    }

    @Override
    public void onReload(ConfigManager newConfig) {
        if (newConfig == null) {
            LogUtil.warn("ConfigManager not set. Using default config file manager.");
            configManager = configManagerFile;
        } else {
            configManager = newConfig;
        }
        // Update variables
        updateVariables();
        // Restart
        api.getAlertManager().reload(configManager);
        api.getDiscordManager().reload();
        api.getSpectateManager().reload();
        api.getViolationDatabaseManager().reload();
        // Don't reload players if the plugin hasn't started yet
        if (!started) return;
        // Reload checks for all players
        for (GrimPlayer player : api.getPlayerDataManager().getEntries()) {
            player.runSafely(() -> player.reload(configManager));
        }
    }

    private void updateVariables() {
        variableReplacements.putIfAbsent("%player%", ReaperUser::getName);
        variableReplacements.putIfAbsent("%uuid%", user -> user.getUniqueId().toString());
        variableReplacements.putIfAbsent("%ping%", user -> user.getTransactionPing() + "");
        variableReplacements.putIfAbsent("%brand%", ReaperUser::getBrand);
        variableReplacements.putIfAbsent("%h_sensitivity%", user -> ((int) Math.round(user.getHorizontalSensitivity() * 200)) + "");
        variableReplacements.putIfAbsent("%v_sensitivity%", user -> ((int) Math.round(user.getVerticalSensitivity() * 200)) + "");
        variableReplacements.putIfAbsent("%fast_math%", user -> !user.isVanillaMath() + "");
        variableReplacements.putIfAbsent("%tps%", user -> String.format("%.2f", api.getPlatformServer().getTPS()));
        variableReplacements.putIfAbsent("%version%", ReaperUser::getVersionName);
        // static variables
        staticReplacements.put("%prefix%", MessageUtil.translateAlternateColorCodes('&', api.getConfigManager().getPrefix()));
        staticReplacements.putIfAbsent("%grim_version%", getReaperVersion());
        staticReplacements.putIfAbsent("%reaper_version%", getReaperVersion());
    }
}
