package ac.reaper.platform.bukkit;

import ac.reaper.ReaperAPI;
import ac.reaper.ReaperExternalAPI;
import ac.reaper.api.ReaperAPIProvider;
import ac.reaper.api.ReaperAbstractAPI;
import ac.reaper.api.event.EventBus;
import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.command.CloudCommandService;
import ac.reaper.internal.platform.bukkit.resolver.BukkitResolverRegistrar;
import ac.reaper.manager.init.Initable;
import ac.reaper.manager.init.start.ExemptOnlinePlayersOnReload;
import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.platform.api.Platform;
import ac.reaper.platform.api.PlatformLoader;
import ac.reaper.platform.api.PlatformServer;
import ac.reaper.platform.api.command.CommandService;
import ac.reaper.platform.api.manager.ItemResetHandler;
import ac.reaper.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.platform.api.manager.PlatformPluginManager;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.player.PlatformPlayerFactory;
import ac.reaper.platform.api.scheduler.PlatformScheduler;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.api.sender.SenderFactory;
import ac.reaper.platform.bukkit.initables.BukkitBStats;
import ac.reaper.platform.bukkit.initables.BukkitEventManager;
import ac.reaper.platform.bukkit.initables.BukkitTickEndEvent;
import ac.reaper.platform.bukkit.manager.BukkitItemResetHandler;
import ac.reaper.platform.bukkit.manager.BukkitMessagePlaceHolderManager;
import ac.reaper.platform.bukkit.manager.BukkitParserDescriptorFactory;
import ac.reaper.platform.bukkit.manager.BukkitPermissionRegistrationManager;
import ac.reaper.platform.bukkit.manager.BukkitPlatformPluginManager;
import ac.reaper.platform.bukkit.player.BukkitPlatformPlayerFactory;
import ac.reaper.platform.bukkit.scheduler.bukkit.BukkitPlatformScheduler;
import ac.reaper.platform.bukkit.scheduler.folia.FoliaPlatformScheduler;
import ac.reaper.platform.bukkit.sender.BukkitSenderFactory;
import ac.reaper.platform.bukkit.utils.placeholder.PlaceholderAPIExpansion;
import ac.reaper.utils.anticheat.LogUtil;
import ac.reaper.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;


public final class ReaperACBukkitLoaderPlugin extends JavaPlugin implements PlatformLoader {

    public static ReaperACBukkitLoaderPlugin LOADER;

    private final LazyHolder<PlatformScheduler> scheduler = LazyHolder.simple(this::createScheduler);
    private final LazyHolder<PacketEventsAPI<?>> packetEvents = LazyHolder.simple(() -> SpigotPacketEventsBuilder.build(this));
    private final LazyHolder<BukkitSenderFactory> senderFactory = LazyHolder.simple(BukkitSenderFactory::new);
    private final LazyHolder<ItemResetHandler> itemResetHandler = LazyHolder.simple(BukkitItemResetHandler::new);
    private final LazyHolder<CommandService> commandService = LazyHolder.simple(this::createCommandService);
    private final CloudCommandAdapter commandAdapter = new BukkitParserDescriptorFactory();

    @Getter private final PlatformPlayerFactory platformPlayerFactory = new BukkitPlatformPlayerFactory();
    @Getter private final PlatformPluginManager pluginManager = new BukkitPlatformPluginManager();
    @Getter private final ReaperPlugin plugin;
    @Getter private final PlatformServer platformServer = new BukkitPlatformServer();
    @Getter private final MessagePlaceHolderManager messagePlaceHolderManager = new BukkitMessagePlaceHolderManager();
    @Getter private final BukkitPermissionRegistrationManager permissionManager = new BukkitPermissionRegistrationManager();

    public ReaperACBukkitLoaderPlugin() {
        BukkitResolverRegistrar registrar = new BukkitResolverRegistrar();
        registrar.registerAll(ReaperAPI.INSTANCE.getExtensionManager());
        this.plugin = registrar.resolvePlugin(this);
    }

    @Override
    public void onLoad() {
        LOADER = this;
        ReaperAPI.INSTANCE.load(this, this.getBukkitInitTasks());
    }

    private Initable[] getBukkitInitTasks() {
        return new Initable[] {
                new ExemptOnlinePlayersOnReload(),
                new BukkitEventManager(),
                new BukkitTickEndEvent(),
                new BukkitBStats(),
                (StartableInitable) () -> {
                    if (BukkitMessagePlaceHolderManager.hasPlaceholderAPI) {
                        new PlaceholderAPIExpansion().register();
                    }
                }
        };
    }

    @Override
    public void onEnable() {
        ReaperAPI.INSTANCE.start();
    }

    @Override
    public void onDisable() {
        ReaperAPI.INSTANCE.stop();
    }

    @Override
    public PlatformScheduler getScheduler() {
        return scheduler.get();
    }

    @Override
    public PacketEventsAPI<?> getPacketEvents() {
        return packetEvents.get();
    }

    @Override
    public ItemResetHandler getItemResetHandler() {
        return itemResetHandler.get();
    }

    @Override
    public CommandService getCommandService() {
        return commandService.get();
    }

    @Override
    public SenderFactory<CommandSender> getSenderFactory() {
        return senderFactory.get();
    }

    @Override
    public void registerAPIService() {
        final ReaperExternalAPI externalAPI = ReaperAPI.INSTANCE.getExternalAPI();
        final EventBus eventBus = externalAPI.getEventBus();
        final ac.reaper.api.plugin.ReaperPlugin context = ReaperAPI.INSTANCE.getReaperPlugin();

        eventBus.subscribe(context, ac.reaper.api.event.events.ReaperJoinEvent.class, (event) -> {
            ac.reaper.api.events.ReaperJoinEvent bukkitEvent =
                    new ac.reaper.api.events.ReaperJoinEvent(event.getUser());

            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });

        eventBus.subscribe(context, ac.reaper.api.event.events.ReaperQuitEvent.class, (event) -> {
            ac.reaper.api.events.ReaperQuitEvent bukkitEvent =
                    new ac.reaper.api.events.ReaperQuitEvent(event.getUser());

            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });

        eventBus.subscribe(context, ac.reaper.api.event.events.ReaperReloadEvent.class, (event) -> {
            ac.reaper.api.events.ReaperReloadEvent bukkitEvent =
                    new ac.reaper.api.events.ReaperReloadEvent(event.isSuccess());

            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });

        eventBus.subscribe(context, ac.reaper.api.event.events.FlagEvent.class, (event) -> {
            ac.reaper.api.events.FlagEvent bukkitEvent =
                    new ac.reaper.api.events.FlagEvent(
                            event.getUser(),
                            event.getCheck(),
                            event.getVerbose()
                    );

            Bukkit.getPluginManager().callEvent(bukkitEvent);

            if (bukkitEvent.isCancelled()) {
                event.setCancelled(true);
            }
        });

        eventBus.subscribe(context, ac.reaper.api.event.events.CommandExecuteEvent.class, (event) -> {
            ac.reaper.api.events.CommandExecuteEvent bukkitEvent =
                    new ac.reaper.api.events.CommandExecuteEvent(
                            event.getUser(),
                            event.getCheck(),
                            event.getVerbose(),
                            event.getCommand()
                    );

            Bukkit.getPluginManager().callEvent(bukkitEvent);

            if (bukkitEvent.isCancelled()) {
                event.setCancelled(true);
            }
        });

        eventBus.subscribe(context, ac.reaper.api.event.events.CompletePredictionEvent.class, (event) -> {
            // Note: New event doesn't have verbose, passing null or check name is standard fallback
            ac.reaper.api.events.CompletePredictionEvent bukkitEvent =
                    new ac.reaper.api.events.CompletePredictionEvent(
                            event.getUser(),
                            event.getCheck(),
                            "",
                            event.getOffset()
                    );

            Bukkit.getPluginManager().callEvent(bukkitEvent);

            if (bukkitEvent.isCancelled()) {
                event.setCancelled(true);
            }
        });

        ReaperAPIProvider.init(externalAPI);
        Bukkit.getServicesManager().register(ReaperAbstractAPI.class, externalAPI, ReaperACBukkitLoaderPlugin.LOADER, ServicePriority.Normal);
    }

    private PlatformScheduler createScheduler() {
        return ReaperAPI.INSTANCE.getPlatform() == Platform.FOLIA ? new FoliaPlatformScheduler() : new BukkitPlatformScheduler();
    }

    private CommandService createCommandService() {
        try {
            return new CloudCommandService(this::createCloudCommandManager, commandAdapter);
        } catch (Throwable t) {
            LogUtil.warn("CRITICAL: Failed to initialize Command Framework. " +
                    "Reaper will continue to run with no commands.", t);
            return () -> {};
        }
    }

    private CommandManager<Sender> createCloudCommandManager() {
        LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(
                this,
                ExecutionCoordinator.simpleCoordinator(),
                senderFactory.get()
        );
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                manager.registerBrigadier();
                CloudBrigadierManager<Sender, ?> cbm = manager.brigadierManager();
                cbm.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
            } catch (Throwable t) {
                LogUtil.error("Failed to register Brigadier native completions. Falling back to standard completions.", t);
            }
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        return manager;
    }

    public BukkitSenderFactory getBukkitSenderFactory() {
        return LOADER.senderFactory.get();
    }
}
