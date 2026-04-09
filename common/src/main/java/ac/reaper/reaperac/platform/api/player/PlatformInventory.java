package ac.reaper.reaperac.platform.api.player;

import com.github.retrooper.packetevents.protocol.item.ItemStack;

public interface PlatformInventory {
    ItemStack getItemInHand();

    ItemStack getItemInOffHand();

    ItemStack getStack(int bukkitSlot, int vanillaSlot);

    ItemStack getHelmet();

    ItemStack getChestplate();

    ItemStack getLeggings();

    ItemStack getBoots();

    ItemStack[] getContents();

    String getOpenInventoryKey();

    /**
     * Native platform registry key for the current main-hand item (for example, "minecraft:diamond_pickaxe").
     * Returns null when unavailable.
     */
    default String getNativeMainHandItemKey() {
        return null;
    }

    /**
     * Native main-hand destroy speed for the given block key (for example, "minecraft:stone").
     * Returns null when unavailable.
     */
    default Float getNativeMainHandDestroySpeed(String blockKey) {
        return null;
    }

    /**
     * Native main-hand "correct tool for drops" for the given block key (for example, "minecraft:stone").
     * Returns null when unavailable.
     */
    default Boolean isNativeMainHandCorrectToolForDrops(String blockKey) {
        return null;
    }
}
