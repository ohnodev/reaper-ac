package ac.reaper.platform.fabric.mc1216.command;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.reaper.platform.fabric.sender.FabricSenderFactory;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;

public class Fabric1212PlayerSelectorAdapter extends Fabric1161PlayerSelectorAdapter {

    public Fabric1212PlayerSelectorAdapter(SinglePlayerSelector fabricSelector) {
        super(fabricSelector);
    }

    // 1.21.2 .getCommandSource() moves from entity to player
    @Override
    public Sender getSinglePlayer() {
        return ((FabricSenderFactory) ReaperAPI.INSTANCE.getSenderFactory()).wrap(fabricSelector.single().createCommandSourceStack());
    }
}
