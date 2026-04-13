package ac.reaper.reaperac.checks.impl.prediction;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.api.config.ConfigManager;
import ac.reaper.reaperac.api.event.events.CompletePredictionEvent;
import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.concurrent.atomic.AtomicInteger;

@CheckData(name = "Simulation", decay = 0.02)
public class OffsetHandler extends Check implements PostPredictionCheck {
    private static final AtomicInteger flags = new AtomicInteger(0);
    private static final boolean SIM_PACKET_TRACE = Boolean.getBoolean("grim.simulationPacketTrace");
    private static final long SIM_PACKET_TRACE_COOLDOWN_MS = 2000L;
    // Config
    private double setbackDecayMultiplier;
    private double threshold;
    private double immediateSetbackThreshold;
    private double maxAdvantage;
    private double maxCeiling;
    private double setbackViolationThreshold;
    // Current advantage gained
    private double advantageGained = 0;
    private long lastSimPacketTraceAt;

    public OffsetHandler(GrimPlayer player) {
        super(player);
    }

    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!predictionComplete.isChecked()) return;

        double offset = predictionComplete.getOffset();

        CompletePredictionEvent completePredictionEvent = new CompletePredictionEvent(player, this, offset);
        GrimAPI.INSTANCE.getEventBus().post(completePredictionEvent);

        if (completePredictionEvent.isCancelled()) return;

        if ((offset >= threshold || offset >= immediateSetbackThreshold)) {
            advantageGained += offset;
            giveOffsetLenienceNextTick(offset);

            synchronized (flags) {
                int flagId = (flags.get() & 255) + 1; // 1-256 as possible values

                String humanFormattedOffset;
                if (offset < 0.001) { // 1.129E-3
                    humanFormattedOffset = String.format("%.4E", offset);
                    // Squeeze out an extra digit here by E-03 to E-3
                    humanFormattedOffset = humanFormattedOffset.replace("E-0", "E-");
                } else {
                    // 0.00112945678 -> .001129
                    humanFormattedOffset = String.format("%6f", offset);
                    // I like the leading zero, but removing it lets us add another digit to the end
                    humanFormattedOffset = humanFormattedOffset.replace("0.", ".");
                }

                String verbose = humanFormattedOffset + " /gl " + flagId;
                if (flag(verbose)) {
                    maybeLogSimulationPacketTrace(offset, flagId);
                    if (alert(verbose)) {
                        flags.incrementAndGet(); // This debug was sent somewhere
                        predictionComplete.setIdentifier(flagId);
                    }

                    if ((advantageGained >= maxAdvantage || offset >= immediateSetbackThreshold)
                            && violations >= setbackViolationThreshold) {
                        player.getSetbackTeleportUtil().executeViolationSetback();
                    }
                }
            }

            advantageGained = Math.min(advantageGained, maxCeiling);
        } else {
            advantageGained *= setbackDecayMultiplier;
        }

        removeOffsetLenience();
    }

    private void giveOffsetLenienceNextTick(double offset) {
        // Don't let players carry more than 1 offset into the next tick
        // (I was seeing cheats try to carry 1,000,000,000 offset into the next tick!)
        //
        // This value so that setting back with high ping doesn't allow players to gather high client velocity
        double minimizedOffset = Math.min(offset, 1);

        // Normalize offsets
        player.uncertaintyHandler.lastHorizontalOffset = minimizedOffset;
        player.uncertaintyHandler.lastVerticalOffset = minimizedOffset;
    }

    private void removeOffsetLenience() {
        player.uncertaintyHandler.lastHorizontalOffset = 0;
        player.uncertaintyHandler.lastVerticalOffset = 0;
    }

    @Override
    public void onReload(ConfigManager config) {
        setbackDecayMultiplier = config.getDoubleElse("Simulation.setback-decay-multiplier", 0.999);
        threshold = config.getDoubleElse("Simulation.threshold", 0.001);
        immediateSetbackThreshold = config.getDoubleElse("Simulation.immediate-setback-threshold", 0.1);
        maxAdvantage = config.getDoubleElse("Simulation.max-advantage", 1);
        maxCeiling = config.getDoubleElse("Simulation.max-ceiling", 4);
        setbackViolationThreshold = config.getDoubleElse("Simulation.setback-violation-threshold", 1);
        if (maxAdvantage == -1) maxAdvantage = Double.MAX_VALUE;
        if (immediateSetbackThreshold == -1) immediateSetbackThreshold = Double.MAX_VALUE;
    }

    public boolean doesOffsetFlag(double offset) {
        return offset >= threshold;
    }

    private void maybeLogSimulationPacketTrace(double offset, int flagId) {
        if (!SIM_PACKET_TRACE) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastSimPacketTraceAt < SIM_PACKET_TRACE_COOLDOWN_MS) {
            return;
        }
        lastSimPacketTraceAt = now;

        LogUtil.warn(String.format(
                "[SimulationTrace] player=%s uuid=%s version=%s protocol=%d offset=%.6f gl=%d " +
                        "pkt=%s ageMs=%d hasPos=%s hasRot=%s onGround=%s hCollision=%s teleportAccept=%s " +
                        "move=(%.3f,%.3f,%.3f yaw=%.2f pitch=%.2f) " +
                        "statePos=(%.3f,%.3f,%.3f) claimedPos=(%.3f,%.3f,%.3f) stateOnGround=%s claimedOnGround=%s " +
                        "supportPos=%s supportOnGround=%s feetBlock=%s headBlock=%s " +
                        "softH=%s hardH=%s vertCol=%s step=%s slimeStep=%s nearFluid=%s nearGlitch=%s ogUncertain=%s",
                player.getName(),
                player.user.getUUID(),
                player.getClientVersion().getReleaseName(),
                player.getClientVersion().getProtocolVersion(),
                offset,
                flagId,
                player.packetStateData.lastMovementPacketType,
                Math.max(0L, now - player.packetStateData.lastMovementPacketAtMs),
                player.packetStateData.lastMovementHadPosition,
                player.packetStateData.lastMovementHadRotation,
                player.packetStateData.lastMovementOnGround,
                player.packetStateData.lastMovementHorizontalCollision,
                player.packetStateData.lastMovementWasTeleportAccept,
                player.packetStateData.lastMovementX,
                player.packetStateData.lastMovementY,
                player.packetStateData.lastMovementZ,
                player.packetStateData.lastMovementYaw,
                player.packetStateData.lastMovementPitch,
                player.x,
                player.y,
                player.z,
                player.packetStateData.lastClaimedPosition.getX(),
                player.packetStateData.lastClaimedPosition.getY(),
                player.packetStateData.lastClaimedPosition.getZ(),
                player.onGround,
                player.packetStateData.packetPlayerOnGround,
                formatSupportPos(player.mainSupportingBlockData.blockPos()),
                player.mainSupportingBlockData.onGround(),
                describeBlockAt(player, player.x, player.y - 0.01, player.z),
                describeBlockAt(player, player.x, player.y + 1.62, player.z),
                player.softHorizontalCollision,
                player.horizontalCollision,
                player.verticalCollision,
                player.uncertaintyHandler.isStepMovement,
                player.uncertaintyHandler.isSteppingOnSlime,
                player.pointThreeEstimator.isNearFluid,
                player.uncertaintyHandler.isNearGlitchyBlock,
                player.uncertaintyHandler.onGroundUncertain
        ));
    }

    private static String formatSupportPos(Vector3i pos) {
        if (pos == null) {
            return "null";
        }
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String describeBlockAt(GrimPlayer player, double x, double y, double z) {
        WrappedBlockState state = player.compensatedWorld.getBlock(x, y, z);
        if (state == null) {
            return "null";
        }
        return state.getType() + "#" + state.getGlobalId();
    }
}
