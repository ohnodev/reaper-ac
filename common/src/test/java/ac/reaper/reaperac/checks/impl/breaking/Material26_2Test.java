package ac.reaper.reaperac.checks.impl.breaking;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for all 26.2 materials (sulfur family, cinnabar family)
 * with every relevant tool type using real PacketEvents types.
 */
class Material26_2Test {

    @TestFactory
    Collection<DynamicTest> allMaterial26_2Tests() {
        try (BlockBreakTestFixture fixture = new BlockBreakTestFixture()) {
            List<DynamicTest> tests = new ArrayList<>();

            StateType[] sulfurBlocks = {
                    StateTypes.SULFUR, StateTypes.POTENT_SULFUR,
                    StateTypes.SULFUR_SLAB, StateTypes.SULFUR_STAIRS, StateTypes.SULFUR_WALL,
                    StateTypes.POLISHED_SULFUR, StateTypes.POLISHED_SULFUR_SLAB,
                    StateTypes.POLISHED_SULFUR_STAIRS, StateTypes.POLISHED_SULFUR_WALL,
                    StateTypes.SULFUR_BRICKS, StateTypes.SULFUR_BRICK_SLAB,
                    StateTypes.SULFUR_BRICK_STAIRS, StateTypes.SULFUR_BRICK_WALL,
                    StateTypes.CHISELED_SULFUR
            };

            StateType[] cinnabarBlocks = {
                    StateTypes.CINNABAR, StateTypes.CINNABAR_SLAB,
                    StateTypes.CINNABAR_STAIRS, StateTypes.CINNABAR_WALL,
                    StateTypes.POLISHED_CINNABAR, StateTypes.POLISHED_CINNABAR_SLAB,
                    StateTypes.POLISHED_CINNABAR_STAIRS, StateTypes.POLISHED_CINNABAR_WALL,
                    StateTypes.CINNABAR_BRICKS, StateTypes.CINNABAR_BRICK_SLAB,
                    StateTypes.CINNABAR_BRICK_STAIRS, StateTypes.CINNABAR_BRICK_WALL,
                    StateTypes.CHISELED_CINNABAR
            };

            record ToolDef(String name, ItemType type, double maxMs) {}
            ToolDef[] pickaxes = {
                    new ToolDef("wooden_pickaxe", ItemTypes.WOODEN_PICKAXE, 1200),
                    new ToolDef("stone_pickaxe", ItemTypes.STONE_PICKAXE, 800),
                    new ToolDef("iron_pickaxe", ItemTypes.IRON_PICKAXE, 500),
                    new ToolDef("diamond_pickaxe", ItemTypes.DIAMOND_PICKAXE, 400),
                    new ToolDef("netherite_pickaxe", ItemTypes.NETHERITE_PICKAXE, 350),
            };

            // Sulfur family × all pickaxe tiers
            for (StateType block : sulfurBlocks) {
                for (ToolDef tool : pickaxes) {
                    ItemStack stack = ItemStack.builder().type(tool.type).build();
                    double predicted = fixture.predictedBreakTimeMs(stack, block);
                    String label = block.getName() + " + " + tool.name;
                    tests.add(DynamicTest.dynamicTest(label, () -> {
                        assertTrue(predicted > 0, label + ": predicted must be positive");
                        assertTrue(predicted <= tool.maxMs,
                                label + ": predicted=" + predicted + "ms exceeds max=" + tool.maxMs + "ms");
                    }));
                }
            }

            // Cinnabar family × stone pickaxe
            ItemStack stonePick = ItemStack.builder().type(ItemTypes.STONE_PICKAXE).build();
            for (StateType block : cinnabarBlocks) {
                double predicted = fixture.predictedBreakTimeMs(stonePick, block);
                String label = block.getName() + " + stone_pickaxe";
                tests.add(DynamicTest.dynamicTest(label, () ->
                        assertTrue(predicted <= 800,
                                label + ": predicted=" + predicted + "ms exceeds 800ms")));
            }

            // Bare hand on sulfur/cinnabar should be slow
            for (StateType block : new StateType[]{StateTypes.SULFUR, StateTypes.CINNABAR}) {
                double predicted = fixture.predictedBreakTimeMs(ItemStack.EMPTY, block);
                String label = block.getName() + " + bare_hand";
                tests.add(DynamicTest.dynamicTest(label, () ->
                        assertTrue(predicted >= 7000,
                                label + ": bare hand should be >= 7000ms, got " + predicted + "ms")));
            }

            return tests;
        }
    }
}
