package ac.reaper.reaperac.checks.impl.breaking;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for block-break timing alignment between Reaper's
 * {@code FastBreak} check and vanilla Minecraft 26.2 mining mechanics.
 *
 * <p>Each test constructs a deterministic {@link BlockBreakTestFixture},
 * sets a tool, and replays a dig sequence through the real
 * {@link ac.reaper.reaperac.utils.nmsutil.BlockBreakSpeed} math.
 *
 * <h3>Adding a new block/tool scenario</h3>
 * <ol>
 *   <li>Add block factory in {@link BlockScenarios} — call
 *       {@code mockBlock(name, hardness, requiresCorrectTool)}.</li>
 *   <li>If the tool needs a TOOL component, use
 *       {@code pickaxeWithToolComponent(speed, tierAttributes...)}.</li>
 *   <li>If using tag-inference instead of a TOOL component, call
 *       {@code fixture.addSyncedBlockTag(tagLocation, Set.of(blockNames))} to
 *       register the tag before computing damage.</li>
 *   <li>Write a test that calls {@code fixture.predictedBreakTimeMs(tool, block)}
 *       and/or {@code DigReplay.replayBreak(fixture, tool, block, delayMs)} and
 *       asserts the expected cancel/no-cancel outcome.</li>
 * </ol>
 *
 * <h3>Running</h3>
 * <pre>./gradlew :common:test</pre>
 */
class FastBreakReplayTest {

    private BlockBreakTestFixture fixture;
    private StateType sulfur;

    @BeforeEach
    void setUp() {
        fixture = new BlockBreakTestFixture();
        sulfur = BlockScenarios.sulfur();
    }

    @AfterEach
    void tearDown() {
        if (fixture != null) fixture.close();
    }

    // -----------------------------------------------------------------------
    // Sulfur + pickaxe regression — the scenario from production logs
    // -----------------------------------------------------------------------

    @Test
    void sulfur_stonePickaxe_withToolComponent_shouldNotCancel() {
        ItemStack pick = BlockScenarios.stonePickaxe();
        fixture.setHeldItem(pick);

        double predicted = fixture.predictedBreakTimeMs(pick, sulfur);
        // Vanilla: sulfur (hardness=1.5) with stone pickaxe (speed=4.0) → ~600ms
        assertTrue(predicted < 2000,
                "predicted time should be well under 2s, got " + predicted + "ms");

        long breakDelay = (long) predicted + 100;
        DigReplay.Result result = DigReplay.replayBreak(fixture, pick, sulfur, breakDelay);

        assertFalse(result.cancelled(),
                "Legit sulfur break with stone pickaxe should NOT be cancelled. " + result.summary());
    }

    @Test
    void sulfur_stonePickaxe_bareType_tagInference_shouldNotCancel() {
        ItemStack pick = BlockScenarios.pickaxeBareType(
                ItemTypes.ItemAttribute.STONE_TIER, ItemTypes.ItemAttribute.PICKAXE);
        fixture.setHeldItem(pick);

        // Without TOOL component, speed stays at 1.0 but canHarvest should still be true
        // via inferCorrectToolFromTags → predicted ~2250ms, not 7500ms
        // We need the tag manager to report sulfur as mineable/pickaxe
        fixture.addSyncedBlockTag(
                com.github.retrooper.packetevents.resources.ResourceLocation.minecraft("mineable/pickaxe"),
                java.util.Set.of("sulfur"));

        double predicted = fixture.predictedBreakTimeMs(pick, sulfur);
        assertTrue(predicted < 5000,
                "tag-inferred predicted time must not be the bare-hand 7500ms, got " + predicted + "ms");

        long breakDelay = (long) predicted + 100;
        DigReplay.Result result = DigReplay.replayBreak(fixture, pick, sulfur, breakDelay);

        assertFalse(result.cancelled(),
                "Sulfur with bare-type pickaxe (tag inference) should NOT cancel. " + result.summary());
    }

    @Test
    void sulfur_ironPickaxe_shouldNotCancel() {
        ItemStack pick = BlockScenarios.ironPickaxe();
        fixture.setHeldItem(pick);

        long breakDelay = (long) fixture.predictedBreakTimeMs(pick, sulfur) + 100;
        DigReplay.Result result = DigReplay.replayBreak(fixture, pick, sulfur, breakDelay);

        assertFalse(result.cancelled(),
                "Legit sulfur break with iron pickaxe should NOT cancel. " + result.summary());
    }

    @Test
    void sulfur_diamondPickaxe_shouldNotCancel() {
        ItemStack pick = BlockScenarios.diamondPickaxe();
        fixture.setHeldItem(pick);

        long breakDelay = (long) fixture.predictedBreakTimeMs(pick, sulfur) + 100;
        DigReplay.Result result = DigReplay.replayBreak(fixture, pick, sulfur, breakDelay);

        assertFalse(result.cancelled(),
                "Legit sulfur break with diamond pickaxe should NOT cancel. " + result.summary());
    }

    @Test
    void sulfur_woodenPickaxe_shouldNotCancel() {
        ItemStack pick = BlockScenarios.woodenPickaxe();
        fixture.setHeldItem(pick);

        long breakDelay = (long) fixture.predictedBreakTimeMs(pick, sulfur) + 100;
        DigReplay.Result result = DigReplay.replayBreak(fixture, pick, sulfur, breakDelay);

        assertFalse(result.cancelled(),
                "Legit sulfur break with wooden pickaxe should NOT cancel. " + result.summary());
    }

    // -----------------------------------------------------------------------
    // Too-fast control — harness validity: FastBreak SHOULD cancel
    // -----------------------------------------------------------------------

    @Test
    void sulfur_stonePickaxe_tooFast_shouldCancel() {
        ItemStack pick = BlockScenarios.stonePickaxe();
        fixture.setHeldItem(pick);

        // Break in 50ms when predicted is ~600ms → diff ~550, two breaks push balance > 1000
        DigReplay.Result first = DigReplay.replayBreak(fixture, pick, sulfur, 50);
        DigReplay.Result second = DigReplay.replayBreak(fixture, pick, sulfur, 50, first.balanceAfter());

        assertTrue(second.cancelled(),
                "Two ultra-fast breaks should accumulate enough balance to cancel. " + second.summary());
    }

    @Test
    void sulfur_bareHand_legitTiming_shouldNotCancel() {
        ItemStack empty = BlockScenarios.emptyHand();
        fixture.setHeldItem(empty);

        double predicted = fixture.predictedBreakTimeMs(empty, sulfur);
        assertEquals(7500.0, predicted, 1.0,
                "Bare hand on sulfur should predict 7500ms");

        DigReplay.Result result = DigReplay.replayBreak(fixture, empty, sulfur, 7600);
        assertFalse(result.cancelled(),
                "Bare hand at legit timing should NOT cancel. " + result.summary());
    }

    // -----------------------------------------------------------------------
    // Diagnostics: predicted time sanity checks
    // -----------------------------------------------------------------------

    @Test
    void sulfur_predictedTime_withPickaxe_isReasonable() {
        ItemStack pick = BlockScenarios.stonePickaxe();
        double predicted = fixture.predictedBreakTimeMs(pick, sulfur);

        // Stone pickaxe on sulfur: vanilla is ~0.6s = 600ms
        assertTrue(predicted >= 400 && predicted <= 1000,
                "Stone pickaxe on sulfur should predict 400-1000ms, got " + predicted);
    }

    @Test
    void sulfur_predictedTime_bareHand_is7500ms() {
        ItemStack empty = BlockScenarios.emptyHand();
        double predicted = fixture.predictedBreakTimeMs(empty, sulfur);
        assertEquals(7500.0, predicted, 1.0,
                "Bare hand on sulfur (hardness=1.5, requiresCorrectTool=true) should be 7500ms");
    }
}
