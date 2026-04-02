package ac.reaper.platform.bukkit.initables;

import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.platform.bukkit.ReaperACBukkitLoaderPlugin;
import ac.reaper.utils.anticheat.Constants;
import io.github.retrooper.packetevents.bstats.bukkit.Metrics;

public class BukkitBStats implements StartableInitable {
    @Override
    public void start() {
        try {
            new Metrics(ReaperACBukkitLoaderPlugin.LOADER, Constants.BSTATS_PLUGIN_ID);
        } catch (Exception ignored) {}
    }
}
