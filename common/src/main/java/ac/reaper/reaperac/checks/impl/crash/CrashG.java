package ac.reaper.reaperac.checks.impl.crash;

import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.BlockPlaceCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.BlockBreak;
import ac.reaper.reaperac.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

@CheckData(name = "CrashG", description = "Sent negative sequence id")
public class CrashG extends BlockPlaceCheck {

    public CrashG(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM && isSupportedVersion()) {
            WrapperPlayClientUseItem use = new WrapperPlayClientUseItem(event);
            if (use.getSequence() < 0) {
                flagAndAlert();
                event.setCancelled(true);
                player.onPacketCancel();
            }
        }
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.sequence < 0 && isSupportedVersion()) {
            flagAndAlert();
            blockBreak.cancel();
        }
    }

    @Override
    public void onBlockPlace(BlockPlace place) {
        if (place.sequence < 0 && isSupportedVersion()) {
            flagAndAlert();
            place.resync();
        }
    }

    private boolean isSupportedVersion() {
        player.getClientVersion();
        return true;
    }

}
