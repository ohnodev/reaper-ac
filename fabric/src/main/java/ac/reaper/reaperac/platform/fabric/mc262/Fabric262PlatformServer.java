package ac.reaper.reaperac.platform.fabric.mc262;

import ac.reaper.reaperac.platform.api.sender.Sender;
import ac.reaper.reaperac.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import net.minecraft.commands.CommandSourceStack;

public class Fabric262PlatformServer extends AbstractFabricPlatformServer {
    @Override
    public void dispatchCommand(Sender sender, String command) {
        CommandSourceStack commandSource = GrimACFabricLoaderPlugin.LOADER.getFabricSenderFactory().unwrap(sender);
        GrimACFabricLoaderPlugin.FABRIC_SERVER.getCommands().performPrefixedCommand(commandSource, command);
    }

    @Override
    public double getTPS() {
        double smoothedTickTime = GrimACFabricLoaderPlugin.FABRIC_SERVER.getCurrentSmoothedTickTime();
        double configuredTickRate = GrimACFabricLoaderPlugin.FABRIC_SERVER.tickRateManager().tickrate();
        if (smoothedTickTime <= 0 || Double.isNaN(smoothedTickTime)) {
            return configuredTickRate;
        }
        return Math.min(1000.0 / smoothedTickTime, configuredTickRate);
    }
}
