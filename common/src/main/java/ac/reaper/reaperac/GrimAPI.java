package ac.reaper.reaperac;

import ac.grim.reaperac.api.event.EventBus;
import ac.grim.reaperac.api.plugin.GrimPlugin;
import ac.grim.reaperac.internal.plugin.resolver.GrimExtensionManager;
import ac.grim.reaperac.internal.event.OptimizedEventBus;
import ac.reaper.reaperac.manager.AlertManagerImpl;
import ac.reaper.reaperac.manager.DiscordManager;
import ac.reaper.reaperac.manager.InitManager;
import ac.reaper.reaperac.manager.SpectateManager;
import ac.reaper.reaperac.manager.TickManager;
import ac.reaper.reaperac.manager.config.BaseConfigManager;
import ac.reaper.reaperac.manager.init.Initable;
import ac.reaper.reaperac.manager.violationdatabase.ViolationDatabaseManager;
import ac.reaper.reaperac.platform.api.Platform;
import ac.reaper.reaperac.platform.api.PlatformLoader;
import ac.reaper.reaperac.platform.api.PlatformServer;
import ac.reaper.reaperac.platform.api.command.CommandService;
import ac.reaper.reaperac.platform.api.manager.ItemResetHandler;
import ac.reaper.reaperac.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.reaperac.platform.api.manager.PlatformPluginManager;
import ac.reaper.reaperac.platform.api.player.PlatformPlayerFactory;
import ac.reaper.reaperac.platform.api.scheduler.PlatformScheduler;
import ac.reaper.reaperac.platform.api.sender.SenderFactory;
import ac.reaper.reaperac.utils.anticheat.PlayerDataManager;
import ac.reaper.reaperac.utils.common.arguments.CommonGrimArguments;
import ac.reaper.reaperac.utils.reflection.ReflectionUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;


@Getter
public final class GrimAPI {
    public static final GrimAPI INSTANCE = new GrimAPI();

    @Getter
    private final Platform platform = detectPlatform();
    private final BaseConfigManager configManager;
    private final AlertManagerImpl alertManager;
    private final SpectateManager spectateManager;
    private final DiscordManager discordManager;
    private final PlayerDataManager playerDataManager;
    private final TickManager tickManager;
    private final GrimExtensionManager extensionManager;
    private final EventBus eventBus;
    private final GrimExternalAPI externalAPI;
    private ViolationDatabaseManager violationDatabaseManager;
    private PlatformLoader loader;
    @Getter
    private InitManager initManager;
    private boolean initialized = false;

    private GrimAPI() {
        this.configManager = new BaseConfigManager();
        this.alertManager = new AlertManagerImpl();
        this.spectateManager = new SpectateManager();
        this.discordManager = new DiscordManager();
        this.playerDataManager = new PlayerDataManager();
        this.tickManager = new TickManager();
        this.extensionManager = new GrimExtensionManager();
        this.eventBus = new OptimizedEventBus(extensionManager);
        this.externalAPI = new GrimExternalAPI(this);
    }

    // the order matters
    private static Platform detectPlatform() {
        Platform override = CommonGrimArguments.PLATFORM_OVERRIDE.value();
        if (override != null) return override;
        if (ReflectionUtils.hasClass("io.papermc.paper.threadedregions.RegionizedServer")) return Platform.FOLIA;
        if (ReflectionUtils.hasClass("org.bukkit.Bukkit")) return Platform.BUKKIT;
        if (ReflectionUtils.hasClass("net.fabricmc.loader.api.FabricLoader")) return Platform.FABRIC;
        throw new IllegalStateException("Unknown platform!");
    }

    public void load(PlatformLoader platformLoader, Initable... platformSpecificInitables) {
        this.loader = platformLoader;
        this.violationDatabaseManager = new ViolationDatabaseManager(getGrimPlugin());
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

    public GrimPlugin getGrimPlugin() {
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
            throw new IllegalStateException("GrimAPI has not been initialized!");
        }
    }

    public GrimExtensionManager getExtensionManager() {
        return extensionManager;
    }
}
