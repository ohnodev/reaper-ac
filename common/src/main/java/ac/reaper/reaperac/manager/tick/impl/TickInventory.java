package ac.reaper.reaperac.manager.tick.impl;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.manager.tick.Tickable;
import ac.reaper.reaperac.player.GrimPlayer;

public class TickInventory implements Tickable {
    @Override
    public void tick() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.inventory.inventory.getInventoryStorage().tickWithBukkit();
        }
    }
}
