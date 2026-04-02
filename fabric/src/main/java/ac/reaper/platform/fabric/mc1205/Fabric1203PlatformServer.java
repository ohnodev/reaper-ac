package ac.reaper.platform.fabric.mc1205;

import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1194.Fabric1190PlatformServer;

public class Fabric1203PlatformServer extends Fabric1190PlatformServer {

    // TODO (Cross-platform) implement proper bukkit equivalent for getting TPS over time
    @Override
    public double getTPS() {
        double smoothedTickTime = ReaperACFabricLoaderPlugin.FABRIC_SERVER.getCurrentSmoothedTickTime();
        double configuredTickRate = ReaperACFabricLoaderPlugin.FABRIC_SERVER.tickRateManager().tickrate();
        if (smoothedTickTime <= 0 || Double.isNaN(smoothedTickTime)) {
            return configuredTickRate;
        }
        return Math.min(1000.0 / smoothedTickTime, configuredTickRate);
    }

}
