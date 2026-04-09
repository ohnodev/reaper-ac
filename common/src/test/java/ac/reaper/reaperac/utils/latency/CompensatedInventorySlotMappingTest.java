package ac.reaper.reaperac.utils.latency;

import ac.reaper.reaperac.utils.inventory.Inventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompensatedInventorySlotMappingTest {

    @Test
    void vanillaInvToStorageSlot_boundary_mapping_is_exact() {
        assertEquals(Inventory.HOTBAR_OFFSET, CompensatedInventory.vanillaInvToStorageSlot(0));
        assertEquals(Inventory.HOTBAR_OFFSET + 8, CompensatedInventory.vanillaInvToStorageSlot(8));
        assertEquals(9, CompensatedInventory.vanillaInvToStorageSlot(9));
        assertEquals(35, CompensatedInventory.vanillaInvToStorageSlot(35));
        assertEquals(Inventory.SLOT_BOOTS, CompensatedInventory.vanillaInvToStorageSlot(36));
        assertEquals(Inventory.SLOT_LEGGINGS, CompensatedInventory.vanillaInvToStorageSlot(37));
        assertEquals(Inventory.SLOT_CHESTPLATE, CompensatedInventory.vanillaInvToStorageSlot(38));
        assertEquals(Inventory.SLOT_HELMET, CompensatedInventory.vanillaInvToStorageSlot(39));
        assertEquals(Inventory.SLOT_OFFHAND, CompensatedInventory.vanillaInvToStorageSlot(40));
        assertEquals(-1, CompensatedInventory.vanillaInvToStorageSlot(41));
    }
}
