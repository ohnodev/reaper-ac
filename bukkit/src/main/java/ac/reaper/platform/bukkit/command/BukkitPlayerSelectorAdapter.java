package ac.reaper.platform.bukkit.command;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.api.command.PlayerSelector;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.platform.bukkit.sender.BukkitSenderFactory;
import lombok.RequiredArgsConstructor;
import org.incendo.cloud.bukkit.data.SinglePlayerSelector;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class BukkitPlayerSelectorAdapter implements PlayerSelector {
    private final SinglePlayerSelector bukkitSelector;

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Sender getSinglePlayer() {
        return ((BukkitSenderFactory) ReaperAPI.INSTANCE.getSenderFactory()).map(bukkitSelector.single());
    }

    @Override
    public Collection<Sender> getPlayers() {
        return Collections.singletonList(((BukkitSenderFactory) ReaperAPI.INSTANCE.getSenderFactory()).map(bukkitSelector.single()));
    }

    @Override
    public String inputString() {
        return bukkitSelector.inputString();
    }
}
