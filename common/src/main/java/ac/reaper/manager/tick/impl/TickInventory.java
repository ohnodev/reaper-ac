package ac.reaper.manager.tick.impl;

import ac.reaper.ReaperAPI;
import ac.reaper.manager.tick.Tickable;
import ac.reaper.player.ReaperPlayer;

public class TickInventory implements Tickable {
    @Override
    public void tick() {
        for (ReaperPlayer player : ReaperAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.inventory.inventory.getInventoryStorage().tickWithBukkit();
        }
    }
}
