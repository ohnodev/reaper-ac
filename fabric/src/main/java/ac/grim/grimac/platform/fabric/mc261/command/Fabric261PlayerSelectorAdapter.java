package ac.grim.grimac.platform.fabric.mc261.command;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.grim.grimac.platform.fabric.sender.FabricSenderFactory;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;

public class Fabric261PlayerSelectorAdapter extends Fabric1161PlayerSelectorAdapter {

    public Fabric261PlayerSelectorAdapter(SinglePlayerSelector fabricSelector) {
        super(fabricSelector);
    }

    // 1.21.2+: .getCommandSource() moves from entity to player
    @Override
    public Sender getSinglePlayer() {
        return ((FabricSenderFactory) GrimAPI.INSTANCE.getSenderFactory()).wrap(fabricSelector.single().createCommandSourceStack());
    }
}
