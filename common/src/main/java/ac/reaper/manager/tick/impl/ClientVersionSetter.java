package ac.reaper.manager.tick.impl;

import ac.reaper.ReaperAPI;
import ac.reaper.manager.tick.Tickable;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;

public class ClientVersionSetter implements Tickable {
    @Override
    public void tick() {
        for (ReaperPlayer player : ReaperAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            // channel was somehow closed without us getting a disconnect event
            if (!ChannelHelper.isOpen(player.user.getChannel())) {
                ReaperAPI.INSTANCE.getPlayerDataManager().onDisconnect(player.user);
                continue;
            }

            player.pollData();
        }
    }
}
