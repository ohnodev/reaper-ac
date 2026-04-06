package ac.reaper.reaperac.checks.impl.crash;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSelectBundleItem;

@CheckData(name = "CrashI")
public class CrashI extends Check implements PacketCheck {
    public CrashI(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.SELECT_BUNDLE_ITEM) {
            try {
                int selectedItemIndex;
                try {
                    selectedItemIndex = new WrapperPlayClientSelectBundleItem(event).getSelectedItemIndex();
                } catch (IllegalArgumentException e) {
                    // thanks packetevents!
                    if (e.getMessage().startsWith("Invalid selectedItemIndex: ")) {
                        selectedItemIndex = Integer.parseInt(e.getMessage().substring(27));
                    } else {
                        throw e;
                    }
                }

                if (selectedItemIndex < -1) {
                    flagAndAlert("selectedItemIndex=" + selectedItemIndex);
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            } catch (Exception e) {
            }
        }
    }
}
