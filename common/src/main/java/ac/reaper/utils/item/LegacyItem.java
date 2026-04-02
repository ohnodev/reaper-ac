package ac.reaper.utils.item;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.latency.CompensatedWorld;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

public class LegacyItem extends ItemBehaviour {

    public static final LegacyItem INSTANCE = new LegacyItem();

    @Override
    public boolean canUse(ItemStack item, CompensatedWorld world, ReaperPlayer player, InteractionHand hand) {
        return false; // move legacy code that is responsible for handling item use from PacketPlayerDigging here??
    }

}
