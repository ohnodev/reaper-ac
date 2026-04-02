package ac.reaper;

import ac.reaper.api.ReaperAbstractAPI;
import ac.reaper.api.ReaperUser;
import ac.reaper.api.alerts.AlertManager;
import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.event.EventBus;
import ac.reaper.api.event.events.ReaperReloadEvent;
import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.manager.config.ConfigManagerFileImpl;
import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.LogUtil;
import ac.reaper.utils.anticheat.MessageUtil;
import ac.reaper.utils.common.ConfigReloadObserver;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

//This is used for reaper's external API. It has its own class just for organization.

public class ReaperExternalAPI implements ReaperAbstractAPI, ConfigReloadObserver, StartableInitable {

    private final ReaperAPI api;
    @Getter
    private final Map<String, Function<ReaperUser, String>> variableReplacements = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, String> staticReplacements = new ConcurrentHashMap<>();
    private final Map<String, Function<Object, Object>> functions = new ConcurrentHashMap<>();
    private final ConfigManagerFileImpl configManagerFile = new ConfigManagerFileImpl();
    private ConfigManager configManager = null;
    private boolean started = false;

    public ReaperExternalAPI(ReaperAPI api) {
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
        if (replacement == null) {
            variableReplacements.remove(string);
        } else {
            variableReplacements.put(string, replacement);
        }
    }

    @Override
    public void registerVariable(String variable, String replacement) {
        if (replacement == null) {
            staticReplacements.remove(variable);
        } else {
            staticReplacements.put(variable, replacement);
        }
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
        return ReaperAPI.INSTANCE.getAlertManager();
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
        return ReaperAPI.INSTANCE.getTickManager().currentTick;
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
            ReaperAPI.INSTANCE.getConfigManager().start();
        } catch (Exception e) {
            LogUtil.error("Failed to start config manager.", e);
        }
    }

    @Override
    public void reload(ConfigManager config) {
        if (config.isLoadedAsync() && started) {
            ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(ReaperAPI.INSTANCE.getReaperPlugin(),
                    () -> successfulReload(config));
        } else {
            successfulReload(config);
        }
    }

    @Override
    public CompletableFuture<Boolean> reloadAsync(ConfigManager config) {
        if (config.isLoadedAsync() && started) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(ReaperAPI.INSTANCE.getReaperPlugin(),
                    () -> future.complete(successfulReload(config)));
            return future;
        }
        return CompletableFuture.completedFuture(successfulReload(config));
    }

    private boolean successfulReload(ConfigManager config) {
        try {
            config.reload();
            ReaperAPI.INSTANCE.getConfigManager().load(config);
            if (started) ReaperAPI.INSTANCE.getConfigManager().start();
            onReload(config);
            if (started)
                ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(ReaperAPI.INSTANCE.getReaperPlugin(),
                        () -> ReaperAPI.INSTANCE.getEventBus().post(new ReaperReloadEvent(true)));
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to reload config", e);
        }
        if (started)
            ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(ReaperAPI.INSTANCE.getReaperPlugin(),
                    () -> ReaperAPI.INSTANCE.getEventBus().post(new ReaperReloadEvent(false)));
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
        ReaperAPI.INSTANCE.getAlertManager().reload(configManager);
        ReaperAPI.INSTANCE.getDiscordManager().reload();
        ReaperAPI.INSTANCE.getSpectateManager().reload();
        ReaperAPI.INSTANCE.getViolationDatabaseManager().reload();
        // Don't reload players if the plugin hasn't started yet
        if (!started) return;
        // Reload checks for all players
        for (ReaperPlayer player : ReaperAPI.INSTANCE.getPlayerDataManager().getEntries()) {
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
        variableReplacements.putIfAbsent("%tps%", user -> String.format("%.2f", ReaperAPI.INSTANCE.getPlatformServer().getTPS()));
        variableReplacements.putIfAbsent("%version%", ReaperUser::getVersionName);
        // static variables
        staticReplacements.put("%prefix%", MessageUtil.translateAlternateColorCodes('&', ReaperAPI.INSTANCE.getConfigManager().getPrefix()));
        staticReplacements.putIfAbsent("%reaper_version%", getReaperVersion());
    }
}
