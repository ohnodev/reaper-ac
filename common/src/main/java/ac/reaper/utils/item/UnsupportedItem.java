package ac.reaper.utils.item;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.latency.CompensatedWorld;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;

public class UnsupportedItem extends ItemBehaviour {

    public static final UnsupportedItem INSTANCE = new UnsupportedItem();

    @Override
    public boolean canUse(ItemStack item, CompensatedWorld world, ReaperPlayer player, InteractionHand hand) {
        return false;
    }

}
