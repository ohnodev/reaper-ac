package ac.reaper.reaperac.checks.impl.breaking;

import ac.reaper.reaperac.utils.nmsutil.BlockBreakSpeed;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;

/**
 * Replays a synthetic dig sequence through the real {@link BlockBreakSpeed#getBlockDamage} math
 * and then applies the same balance logic from {@code FastBreak#onBlockBreak} to determine
 * whether a legitimate break would be cancelled.
 *
 * <p>This avoids needing to construct a real {@code FastBreak} (which requires a running server),
 * while exercising the exact production calculation path that decides cancel-vs-allow.
 *
 * <p>Usage:
 * <pre>
 *   try (BlockBreakTestFixture fx = new BlockBreakTestFixture()) {
 *       fx.setHeldItem(pickaxeStack);
 *       DigReplay.Result r = DigReplay.replayBreak(fx, pickaxeStack, StateTypes.SULFUR, 500);
 *       assertFalse(r.cancelled());
 *   }
 * </pre>
 */
public final class DigReplay {

    private DigReplay() {}

    /** Mirrors FastBreak's internal cancel threshold. */
    private static final double BALANCE_CANCEL_THRESHOLD = 1000.0;

    public record Result(boolean cancelled, double predictedMs, double breakDelayMs,
                         double blockDamage, double balanceAfter) {

        public String summary() {
            return String.format(
                    "cancelled=%s predicted=%.1fms real=%.0fms damage=%.6f balance=%.1f",
                    cancelled, predictedMs, breakDelayMs, blockDamage, balanceAfter);
        }
    }

    /**
     * Simulate a single START → FINISH break and return whether FastBreak would cancel.
     *
     * @param fixture      mock player fixture
     * @param tool         the item stack used for mining
     * @param blockType    block to break (e.g. {@code StateTypes.SULFUR})
     * @param breakDelayMs time between START and FINISH in milliseconds
     * @return result with cancel decision and diagnostics
     */
    public static Result replayBreak(BlockBreakTestFixture fixture, ItemStack tool,
                                     StateType blockType, long breakDelayMs) {
        return replayBreak(fixture, tool, blockType, breakDelayMs, 0.0);
    }

    /**
     * Simulate a single START → FINISH break with a pre-existing balance value.
     */
    public static Result replayBreak(BlockBreakTestFixture fixture, ItemStack tool,
                                     StateType blockType, long breakDelayMs,
                                     double priorBalance) {
        double damage = BlockBreakSpeed.getBlockDamage(fixture.getPlayer(), tool, blockType);

        double predictedMs;
        if (damage <= 0) {
            predictedMs = Double.MAX_VALUE;
        } else {
            predictedMs = Math.ceil(1.0 / damage) * 50.0;
        }

        double diff = predictedMs - breakDelayMs;

        double balance = priorBalance;
        if (diff < 25) {
            balance *= 0.9;
        } else {
            balance += diff;
        }

        boolean cancelled = balance > BALANCE_CANCEL_THRESHOLD;
        return new Result(cancelled, predictedMs, breakDelayMs, damage, balance);
    }
}
