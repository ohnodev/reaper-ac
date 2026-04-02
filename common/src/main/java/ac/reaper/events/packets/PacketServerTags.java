package ac.reaper.events.packets;

import ac.reaper.ReaperAPI;
import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTags;

public class PacketServerTags extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.TAGS || event.getPacketType() == PacketType.Configuration.Server.UPDATE_TAGS) {
            ReaperPlayer player = ReaperAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayServerTags tags = new WrapperPlayServerTags(event);
            final boolean isPlay = event.getPacketType() == PacketType.Play.Server.TAGS;
            if (isPlay) {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.tagManager.handleTagSync(tags));
            } else {
                // This is during configuration stage, player isn't even in the game yet so no need to lag compensate.
                player.tagManager.handleTagSync(tags);
            }
        }
    }
}
