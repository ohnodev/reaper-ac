package ac.reaper.platform.bukkit.initables;

import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import ac.reaper.platform.bukkit.events.PistonEvent;
import ac.reaper.utils.anticheat.LogUtil;
import org.bukkit.Bukkit;

public class BukkitEventManager implements StartableInitable {
    public void start() {
        LogUtil.info("Registering singular bukkit event... (PistonEvent)");

        Bukkit.getPluginManager().registerEvents(new PistonEvent(), ReaperACBukkitLoaderPlugin.LOADER);
    }
}
