package ac.reaper.reaperac.platform.api;

import ac.reaper.reaperac.api.plugin.ReaperPlugin;
import ac.reaper.reaperac.platform.api.manager.ItemResetHandler;
import ac.reaper.reaperac.platform.api.manager.MessagePlaceHolderManager;
import ac.reaper.reaperac.platform.api.manager.PlatformPluginManager;
import ac.reaper.reaperac.platform.api.player.PlatformPlayerFactory;
import ac.reaper.reaperac.platform.api.scheduler.PlatformScheduler;
import ac.reaper.reaperac.platform.api.sender.SenderFactory;
import com.github.retrooper.packetevents.PacketEventsAPI;
import org.jetbrains.annotations.NotNull;

public interface PlatformLoader {
    PlatformScheduler getScheduler();

    PlatformPlayerFactory getPlatformPlayerFactory();

    PacketEventsAPI<?> getPacketEvents();

    ItemResetHandler getItemResetHandler();

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
}
