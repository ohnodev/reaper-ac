package ac.reaper.utils.inventory.slot;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.inventory.InventoryStorage;
import com.github.retrooper.packetevents.protocol.item.ItemStack;

public class ResultSlot extends Slot {

    public ResultSlot(InventoryStorage container, int slot) {
        super(container, slot);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public void onTake(ReaperPlayer player, ItemStack itemStack) {
        // Resync the player's inventory
    }
}
