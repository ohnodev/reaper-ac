package ac.reaper.platform.fabric;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.fabric.initables.FabricBStats;
import ac.reaper.platform.fabric.initables.FabricTickEndEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.util.List;

public class ReaperACFabricEntryPoint implements PreLaunchEntrypoint, ModInitializer {
    @Override
    public void onPreLaunch() {
    }

    @Override
    public void onInitialize() {
        FabricLoader loader = FabricLoader.getInstance();
        String chainLoadEntryPointName = "reaperMainLoad";

        // Collect reaperMainLoad entrypoints and sort by version
        List<ReaperACFabricLoaderPlugin> mainChainLoadEntryPoints = loader.getEntrypoints(chainLoadEntryPointName, ReaperACFabricLoaderPlugin.class);
        mainChainLoadEntryPoints.sort((a, b) -> b.getNativeVersion().getProtocolVersion() - a.getNativeVersion().getProtocolVersion());

        // Get entrypoint for newest sub-version and execute it
        ReaperACFabricLoaderPlugin platformLoader = mainChainLoadEntryPoints.get(0);
        ReaperACFabricLoaderPlugin.LOADER = platformLoader;

        // On Fabric we have to register commands earlier, and cannot register them when server is no longer null
        ReaperAPI.INSTANCE.load(
                platformLoader,
                new FabricBStats(),
                new FabricTickEndEvent()
        );

        ReaperAPI.INSTANCE.getCommandService().registerCommands();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ReaperACFabricLoaderPlugin.FABRIC_SERVER = server;
            ReaperAPI.INSTANCE.start();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            ReaperAPI.INSTANCE.stop();
            platformLoader.getScheduler().shutdown();
        });
    }
}
