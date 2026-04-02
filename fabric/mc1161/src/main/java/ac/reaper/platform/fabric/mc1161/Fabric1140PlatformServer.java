package ac.reaper.platform.fabric.mc1161;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import net.minecraft.commands.CommandSourceStack;

public class Fabric1140PlatformServer extends AbstractFabricPlatformServer {

    @Override
    public void dispatchCommand(Sender sender, String command) {
        CommandSourceStack commandSource = ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().unwrap(sender);
        ReaperACFabricLoaderPlugin.FABRIC_SERVER.getCommands().performCommand(commandSource, command);
    }

    // TODO (Cross-platform) implement proper bukkit equivalent for getting TPS over time
    @Override
    public double getTPS() {
        return Math.min(1000.0 / ReaperACFabricLoaderPlugin.FABRIC_SERVER.getAverageTickTime(), 20.0);
    }
}
