package ac.reaper.reaperac.utils.nmsutil;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import lombok.experimental.UtilityClass;

/**
 * Resolves the effective mining tool stack from a packet-tracked held item and a
 * native platform-derived held item.  Shared between {@link BlockBreakSpeed} and
 * {@link ac.reaper.reaperac.utils.latency.CompensatedInventory}.
 */
@UtilityClass
public class MiningToolUtils {

    /**
     * Returns the best available tool stack for mining simulation.
     *
     * <p>Prefers the native-key stack when the packet-tracked stack is empty, disagrees
     * on item type, or lacks TOOL component metadata for a known mining tool type.
     *
     * @param packetHeld    stack from packet-compensated inventory
     * @param nativeKeyHeld stack derived from the platform's native main-hand key
     * @return the stack to use for tool/mining calculations
     */
    public static ItemStack resolveEffectiveToolStack(ItemStack packetHeld, ItemStack nativeKeyHeld) {
        if (!nativeKeyHeld.isEmpty()) {
            if (packetHeld.isEmpty() || packetHeld.getType() != nativeKeyHeld.getType()) {
                return nativeKeyHeld;
            }
            if (!packetHeld.hasComponent(ComponentTypes.TOOL)) {
                ItemType nativeType = nativeKeyHeld.getType();
                if (nativeType.hasAttribute(ItemTypes.ItemAttribute.PICKAXE)
                        || nativeType.hasAttribute(ItemTypes.ItemAttribute.AXE)
                        || nativeType.hasAttribute(ItemTypes.ItemAttribute.SHOVEL)
                        || nativeType.hasAttribute(ItemTypes.ItemAttribute.HOE)) {
                    return nativeKeyHeld;
                }
            }
        }
        return packetHeld;
    }
}
