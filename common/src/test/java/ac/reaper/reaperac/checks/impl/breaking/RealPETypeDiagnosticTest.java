package ac.reaper.reaperac.checks.impl.breaking;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Diagnostic tests using REAL PacketEvents types to verify the PE data layer
 * provides correct information for the BlockBreakSpeed calculation.
 * These tests catch PE data bugs (missing tags, missing components, etc.).
 */
class RealPETypeDiagnosticTest {

    private BlockBreakTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new BlockBreakTestFixture();
    }

    @AfterEach
    void tearDown() {
        if (fixture != null) fixture.close();
    }

    // ---- StateTypes data checks ----

    @Test
    void sulfur_stateType_exists_and_has_correct_properties() {
        StateType sulfur = StateTypes.SULFUR;
        assertNotNull(sulfur, "StateTypes.SULFUR must exist");
        assertEquals(1.5f, sulfur.getHardness(), 0.01f, "sulfur hardness");
        assertTrue(sulfur.isRequiresCorrectTool(), "sulfur requires correct tool");
    }

    @Test
    void sulfur_is_in_mineable_pickaxe_static_tag() {
        BlockTags tag = BlockTags.MINEABLE_PICKAXE;
        assertNotNull(tag, "BlockTags.MINEABLE_PICKAXE must exist");

        boolean byIdentity = tag.contains(StateTypes.SULFUR);
        boolean byName = tag.getStates().stream()
                .anyMatch(s -> "sulfur".equals(s.getName()));

        assertTrue(byIdentity || byName,
                "SULFUR must be in MINEABLE_PICKAXE (identity=" + byIdentity + ", byName=" + byName + ")");
    }

    @Test
    void sulfur_is_NOT_in_needs_stone_tool() {
        BlockTags tag = BlockTags.NEEDS_STONE_TOOL;
        assertNotNull(tag);

        boolean found = tag.contains(StateTypes.SULFUR)
                || tag.getStates().stream().anyMatch(s -> "sulfur".equals(s.getName()));
        assertFalse(found, "SULFUR must NOT be in NEEDS_STONE_TOOL");
    }

    // ---- ItemTypes data checks ----

    @Test
    void stone_pickaxe_has_pickaxe_and_stone_tier_attributes() {
        ItemType pick = ItemTypes.STONE_PICKAXE;
        assertNotNull(pick, "ItemTypes.STONE_PICKAXE must exist");
        assertTrue(pick.hasAttribute(ItemTypes.ItemAttribute.PICKAXE), "STONE_PICKAXE should have PICKAXE attribute");
        assertTrue(pick.hasAttribute(ItemTypes.ItemAttribute.STONE_TIER), "STONE_PICKAXE should have STONE_TIER attribute");
    }

    // ---- ItemStack TOOL component checks ----

    @Test
    void stone_pickaxe_itemstack_has_tool_component(TestReporter reporter) {
        ItemStack stack = ItemStack.builder()
                .type(ItemTypes.STONE_PICKAXE)
                .build();

        Optional<ItemTool> toolOpt = stack.getComponent(ComponentTypes.TOOL);
        assertTrue(toolOpt.isPresent(),
                "STONE_PICKAXE ItemStack must have a TOOL component from PE base data");

        ItemTool tool = toolOpt.get();
        assertFalse(tool.getRules().isEmpty(),
                "TOOL component should have at least one rule");

        reporter.publishEntry("diag", "[DIAG] STONE_PICKAXE TOOL component:");
        reporter.publishEntry("diag", "  defaultMiningSpeed=" + tool.getDefaultMiningSpeed());
        reporter.publishEntry("diag", "  rules count=" + tool.getRules().size());
        for (int i = 0; i < tool.getRules().size(); i++) {
            ItemTool.Rule rule = tool.getRules().get(i);
            ResourceLocation tagKey = rule.getBlocks().getTagKey();
            reporter.publishEntry("diag", "  rule[" + i + "] tagKey=" + tagKey
                    + " speed=" + rule.getSpeed()
                    + " correctForDrops=" + rule.getCorrectForDrops());
        }
    }

    // ---- End-to-end: real PE types through BlockBreakSpeed ----

    @Test
    void sulfur_with_real_stone_pickaxe_stack_damage_is_correct(TestReporter reporter) {
        ItemStack stack = ItemStack.builder()
                .type(ItemTypes.STONE_PICKAXE)
                .build();
        fixture.setHeldItem(stack);

        double damage = fixture.computeBlockDamage(stack, StateTypes.SULFUR);
        double predicted = fixture.predictedBreakTimeMs(stack, StateTypes.SULFUR);

        reporter.publishEntry("diag", "[DIAG] sulfur + real STONE_PICKAXE stack:");
        reporter.publishEntry("diag", "  damage/tick=" + damage);
        reporter.publishEntry("diag", "  predicted=" + predicted + "ms");

        assertTrue(predicted < 2000,
                "Real STONE_PICKAXE on SULFUR must predict < 2000ms, got " + predicted + "ms. "
                        + "If this is 7500ms, the TOOL component is not matching sulfur.");
    }

    @Test
    void sulfur_with_real_iron_pickaxe_stack_damage_is_correct(TestReporter reporter) {
        ItemStack stack = ItemStack.builder()
                .type(ItemTypes.IRON_PICKAXE)
                .build();
        fixture.setHeldItem(stack);

        double predicted = fixture.predictedBreakTimeMs(stack, StateTypes.SULFUR);
        reporter.publishEntry("diag", "[DIAG] sulfur + real IRON_PICKAXE: predicted=" + predicted + "ms");

        assertTrue(predicted < 1000,
                "Real IRON_PICKAXE on SULFUR must predict < 1000ms, got " + predicted + "ms");
    }

    // ---- 26.2 material matrix ----

    @Test
    void all_sulfur_family_blocks_in_mineable_pickaxe() {
        Set<String> sulfurFamily = Set.of(
                "sulfur", "potent_sulfur", "sulfur_slab", "sulfur_stairs", "sulfur_wall",
                "polished_sulfur", "polished_sulfur_slab", "polished_sulfur_stairs", "polished_sulfur_wall",
                "sulfur_bricks", "sulfur_brick_slab", "sulfur_brick_stairs", "sulfur_brick_wall",
                "chiseled_sulfur");

        Set<String> inTag = new java.util.HashSet<>();
        for (StateType st : BlockTags.MINEABLE_PICKAXE.getStates()) {
            if (sulfurFamily.contains(st.getName())) {
                inTag.add(st.getName());
            }
        }

        Set<String> missing = new java.util.HashSet<>(sulfurFamily);
        missing.removeAll(inTag);

        assertTrue(missing.isEmpty(),
                "All sulfur family blocks should be in MINEABLE_PICKAXE. Missing: " + missing);
    }
}
