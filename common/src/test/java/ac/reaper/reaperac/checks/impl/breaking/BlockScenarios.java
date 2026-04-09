package ac.reaper.reaperac.checks.impl.breaking;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pre-built block/tool scenarios for dig replay tests.
 * All PE types are mocked to avoid heavy static initialization.
 */
public final class BlockScenarios {

    private BlockScenarios() {}

    // ---- Block factories ----

    /**
     * Create a mock sulfur block: hardness=1.5, requiresCorrectTool=true, name="sulfur".
     */
    public static StateType sulfur() {
        return mockBlock("sulfur", 1.5f, true);
    }

    /**
     * Create a mock stone block: hardness=1.5, requiresCorrectTool=true, name="stone".
     */
    public static StateType stone() {
        return mockBlock("stone", 1.5f, true);
    }

    /**
     * Create a mock dirt block: hardness=0.5, requiresCorrectTool=false, name="dirt".
     */
    public static StateType dirt() {
        return mockBlock("dirt", 0.5f, false);
    }

    public static StateType mockBlock(String name, float hardness, boolean requiresCorrectTool) {
        StateType block = mock(StateType.class);
        when(block.getName()).thenReturn(name);
        when(block.getHardness()).thenReturn(hardness);
        when(block.isRequiresCorrectTool()).thenReturn(requiresCorrectTool);
        StateType.Mapped mapped = mock(StateType.Mapped.class);
        ResourceLocation rl = ResourceLocation.minecraft(name);
        when(mapped.getName()).thenReturn(rl);
        when(block.getMapped()).thenReturn(mapped);
        return block;
    }

    // ---- Pickaxe factories ----

    /**
     * Create a mock pickaxe ItemStack WITH the TOOL component.
     *
     * @param tierAttributes which tier attributes the item has (PICKAXE always included)
     * @param speed          speed multiplier for the mineable/pickaxe tag rule
     */
    public static ItemStack pickaxeWithToolComponent(float speed,
                                                     ItemTypes.ItemAttribute... tierAttributes) {
        ItemType itemType = mockPickaxeType(tierAttributes);

        @SuppressWarnings("unchecked")
        MappedEntitySet<StateType.Mapped> mineablePickaxe = mock(MappedEntitySet.class);
        when(mineablePickaxe.getTagKey()).thenReturn(ResourceLocation.minecraft("mineable/pickaxe"));
        when(mineablePickaxe.getEntities()).thenReturn(null);

        ItemTool.Rule rule = new ItemTool.Rule(mineablePickaxe, speed, true);
        ItemTool tool = new ItemTool(List.of(rule), 1.0f, 1);

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getType()).thenReturn(itemType);
        when(stack.getComponent(any())).thenReturn(Optional.empty());
        // Specifically return the tool component for ComponentTypes.TOOL
        when(stack.hasComponent(any())).thenReturn(false);
        // We need type-safe stubbing for the TOOL component
        stubToolComponent(stack, tool);

        return stack;
    }

    /**
     * Create a mock pickaxe WITHOUT the TOOL component (tests tag-inference fallback).
     */
    public static ItemStack pickaxeBareType(ItemTypes.ItemAttribute... tierAttributes) {
        ItemType itemType = mockPickaxeType(tierAttributes);

        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(false);
        when(stack.getType()).thenReturn(itemType);
        when(stack.getComponent(any())).thenReturn(Optional.empty());
        when(stack.hasComponent(any())).thenReturn(false);

        return stack;
    }

    /** Stone pickaxe with vanilla speed 4.0 */
    public static ItemStack stonePickaxe() {
        return pickaxeWithToolComponent(4.0f,
                ItemTypes.ItemAttribute.STONE_TIER, ItemTypes.ItemAttribute.PICKAXE);
    }

    /** Iron pickaxe with vanilla speed 6.0 */
    public static ItemStack ironPickaxe() {
        return pickaxeWithToolComponent(6.0f,
                ItemTypes.ItemAttribute.IRON_TIER, ItemTypes.ItemAttribute.PICKAXE);
    }

    /** Diamond pickaxe with vanilla speed 8.0 */
    public static ItemStack diamondPickaxe() {
        return pickaxeWithToolComponent(8.0f,
                ItemTypes.ItemAttribute.DIAMOND_TIER, ItemTypes.ItemAttribute.PICKAXE);
    }

    /** Wooden pickaxe with vanilla speed 2.0 */
    public static ItemStack woodenPickaxe() {
        return pickaxeWithToolComponent(2.0f,
                ItemTypes.ItemAttribute.WOOD_TIER, ItemTypes.ItemAttribute.PICKAXE);
    }

    /** Empty hand (no tool) */
    public static ItemStack emptyHand() {
        ItemStack stack = mock(ItemStack.class);
        when(stack.isEmpty()).thenReturn(true);
        when(stack.getType()).thenReturn(mock(ItemType.class));
        when(stack.getComponent(any())).thenReturn(Optional.empty());
        when(stack.hasComponent(any())).thenReturn(false);
        return stack;
    }

    // ---- Internal helpers ----

    private static ItemType mockPickaxeType(ItemTypes.ItemAttribute... tierAttributes) {
        Set<ItemTypes.ItemAttribute> attrs = Set.of(tierAttributes);
        ItemType type = mock(ItemType.class);
        when(type.hasAttribute(any(ItemTypes.ItemAttribute.class))).thenAnswer(inv -> {
            ItemTypes.ItemAttribute attr = inv.getArgument(0);
            return attrs.contains(attr);
        });
        return type;
    }

    @SuppressWarnings("unchecked")
    private static void stubToolComponent(ItemStack stack, ItemTool tool) {
        when(stack.getComponent(ComponentTypes.TOOL)).thenReturn(Optional.of(tool));
        when(stack.hasComponent(ComponentTypes.TOOL)).thenReturn(true);
    }
}
