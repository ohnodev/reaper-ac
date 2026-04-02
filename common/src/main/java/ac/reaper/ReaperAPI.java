package ac.reaper;

import ac.reaper.api.event.EventBus;
import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.internal.plugin.resolver.ReaperExtensionManager;
import ac.reaper.internal.event.OptimizedEventBus;
import ac.reaper.manager.AlertManagerImpl;
import ac.reaper.manager.DiscordManager;
import ac.reaper.manager.InitManager;
import ac.reaper.manager.SpectateManager;
import ac.reaper.manager.TickManager;
import ac.reaper.manager.config.BaseConfigManager;
import ac.reaper.manager.init.Initable;
import ac.reaper.manager.violationdatabase.ViolationDatabaseManager;
import ac.reaper.platform.api.Platform;
import ac.reaper.platform.api.PlatformLoader;
import ac.reaper.platform.api.PlatformServer;
import ac.reaper.platform.api.command.CommandService;
import ac.reaper.platform.api.manager.ItemResetHandler;
import ac.reaper.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.platform.api.manager.PermissionRegistrationManager;
import ac.reaper.platform.api.manager.PlatformPluginManager;
import ac.reaper.platform.api.player.PlatformPlayerFactory;
import ac.reaper.platform.api.scheduler.PlatformScheduler;
import ac.reaper.platform.api.sender.SenderFactory;
import ac.reaper.utils.anticheat.PlayerDataManager;
import ac.reaper.utils.common.arguments.CommonReaperArguments;
import ac.reaper.utils.reflection.ReflectionUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;


@Getter
public final class ReaperAPI {
    public static final ReaperAPI INSTANCE = new ReaperAPI();

    @Getter
    private final Platform platform = detectPlatform();
    private final BaseConfigManager configManager;
    private final AlertManagerImpl alertManager;
    private final SpectateManager spectateManager;
    private final DiscordManager discordManager;
    private final PlayerDataManager playerDataManager;
    private final TickManager tickManager;
    private final ReaperExtensionManager extensionManager;
    private final EventBus eventBus;
    private final ReaperExternalAPI externalAPI;
    private ViolationDatabaseManager violationDatabaseManager;
    private PlatformLoader loader;
    @Getter
    private InitManager initManager;
    private boolean initialized = false;

    private ReaperAPI() {
        this.configManager = new BaseConfigManager();
        this.alertManager = new AlertManagerImpl();
        this.spectateManager = new SpectateManager();
        this.discordManager = new DiscordManager();
        this.playerDataManager = new PlayerDataManager();
        this.tickManager = new TickManager();
        this.extensionManager = new ReaperExtensionManager();
        this.eventBus = new OptimizedEventBus(extensionManager);
        this.externalAPI = new ReaperExternalAPI(this);
    }

    // the order matters
    private static Platform detectPlatform() {
        Platform override = CommonReaperArguments.PLATFORM_OVERRIDE.value();
        if (override != null) return override;
        if (ReflectionUtils.hasClass("io.papermc.paper.threadedregions.RegionizedServer")) return Platform.FOLIA;
        if (ReflectionUtils.hasClass("org.bukkit.Bukkit")) return Platform.BUKKIT;
        if (ReflectionUtils.hasClass("net.fabricmc.loader.api.FabricLoader")) return Platform.FABRIC;
        throw new IllegalStateException("Unknown platform!");
    }

    public void load(PlatformLoader platformLoader, Initable... platformSpecificInitables) {
        this.loader = platformLoader;
        this.violationDatabaseManager = new ViolationDatabaseManager(getReaperPlugin());
        this.initManager = new InitManager(loader.getPacketEvents(), platformSpecificInitables);
        this.initManager.load();
        this.initialized = true;
    }

    public void start() {
        checkInitialized();
        initManager.start();
    }

    public void stop() {
        checkInitialized();
        initManager.stop();
    }

    public PlatformScheduler getScheduler() {
        return loader.getScheduler();
    }

    public PlatformPlayerFactory getPlatformPlayerFactory() {
        return loader.getPlatformPlayerFactory();
    }

    public ReaperPlugin getReaperPlugin() {
        return loader.getPlugin();
    }

    public SenderFactory<?> getSenderFactory() {
        return loader.getSenderFactory();
    }

    public ItemResetHandler getItemResetHandler() {
        return loader.getItemResetHandler();
    }

    public PlatformPluginManager getPluginManager() {
        return loader.getPluginManager();
    }

    public PlatformServer getPlatformServer() {
        return loader.getPlatformServer();
    }

    public @NotNull MessagePlaceHolderManager getMessagePlaceHolderManager() {
        return loader.getMessagePlaceHolderManager();
    }

    public CommandService getCommandService() {
        return loader.getCommandService();
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("ReaperAPI has not been initialized!");
        }
    }

    public PermissionRegistrationManager getPermissionManager() {
        return loader.getPermissionManager();
    }

    public ReaperExtensionManager getExtensionManager() {
        return extensionManager;
    }
}
