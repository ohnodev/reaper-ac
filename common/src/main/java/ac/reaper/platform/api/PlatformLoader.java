package ac.reaper.platform.api;

import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.platform.api.command.CommandService;
import ac.reaper.platform.api.manager.ItemResetHandler;
import ac.reaper.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.platform.api.manager.PermissionRegistrationManager;
import ac.reaper.platform.api.manager.PlatformPluginManager;
import ac.reaper.platform.api.player.PlatformPlayerFactory;
import ac.reaper.platform.api.scheduler.PlatformScheduler;
import ac.reaper.platform.api.sender.SenderFactory;
import com.github.retrooper.packetevents.PacketEventsAPI;
import org.jetbrains.annotations.NotNull;

public interface PlatformLoader {
    PlatformScheduler getScheduler();

    PlatformPlayerFactory getPlatformPlayerFactory();

    PacketEventsAPI<?> getPacketEvents();

    ItemResetHandler getItemResetHandler();

    CommandService getCommandService();

    SenderFactory<?> getSenderFactory();

    ReaperPlugin getPlugin();

    PlatformPluginManager getPluginManager();

    PlatformServer getPlatformServer();

    // Intended for use for platform specific service/API bringup
    // Method will be called when InitManager.load() is called
    void registerAPIService();

    // Used to replace text placeholders in messages
    // Currently only supports PlaceHolderAPI on Bukkit
    @NotNull
    MessagePlaceHolderManager getMessagePlaceHolderManager();

    PermissionRegistrationManager getPermissionManager();
}
