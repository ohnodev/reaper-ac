package ac.reaper.platform.fabric.mc1205;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1194.Fabric1190PlatformServer;
import net.minecraft.commands.CommandSourceStack;

public class Fabric1203PlatformServer extends Fabric1190PlatformServer {

    // TODO (Cross-platform) implement proper bukkit equivalent for getting TPS over time
    @Override
    public double getTPS() {
        return Math.min(1000.0 / ReaperACFabricLoaderPlugin.FABRIC_SERVER.getCurrentSmoothedTickTime(), ReaperACFabricLoaderPlugin.FABRIC_SERVER.tickRateManager().tickrate());
    }

    // Return type changed from int -> void in 1.20.3
    @Override
    public void dispatchCommand(Sender sender, String command) {
        CommandSourceStack commandSource = ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().unwrap(sender);
        ReaperACFabricLoaderPlugin.FABRIC_SERVER.getCommands().performPrefixedCommand(commandSource, command);
    }
}
