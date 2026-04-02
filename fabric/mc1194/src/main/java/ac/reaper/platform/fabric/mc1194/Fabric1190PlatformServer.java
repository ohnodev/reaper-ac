package ac.reaper.platform.fabric.mc1194;

import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1171.Fabric1171PlatformServer;
import net.minecraft.commands.CommandSourceStack;

public class Fabric1190PlatformServer extends Fabric1171PlatformServer {
    @Override
    public void dispatchCommand(Sender sender, String command) {
        CommandSourceStack commandSource = ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().unwrap(sender);
        ReaperACFabricLoaderPlugin.FABRIC_SERVER.getCommands().performPrefixedCommand(commandSource, command);
    }
}
