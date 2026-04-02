package ac.reaper.platform.fabric.mc1161.command;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.api.command.PlayerSelector;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.fabric.sender.FabricSenderFactory;
import org.incendo.cloud.minecraft.modded.data.SinglePlayerSelector;

import java.util.Collection;
import java.util.Collections;

public class Fabric1161PlayerSelectorAdapter implements PlayerSelector {
    protected final SinglePlayerSelector fabricSelector;

    public Fabric1161PlayerSelectorAdapter(SinglePlayerSelector fabricSelector) {
        this.fabricSelector = fabricSelector;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Sender getSinglePlayer() {
        return ((FabricSenderFactory) ReaperAPI.INSTANCE.getSenderFactory()).wrap(fabricSelector.single().createCommandSourceStack());
    }

    @Override
    public Collection<Sender> getPlayers() {
        return Collections.singletonList(getSinglePlayer()); // Assuming your ServerPlayer can be cast to Player
    }

    @Override
    public String inputString() {
        return fabricSelector.inputString();
    }
}
