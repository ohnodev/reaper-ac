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

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@CheckData(name = "Simulation", decay = 0.02)
public class OffsetHandler extends Check implements PostPredictionCheck {
    private static final AtomicInteger flags = new AtomicInteger(0);
    private static final boolean SIM_PACKET_TRACE = Boolean.getBoolean("grim.simulationPacketTrace");
    private static final boolean SIM_PACKET_TRACE_SENSITIVE = Boolean.getBoolean("grim.simulationPacketTraceSensitive");
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
    private volatile long lastSimPacketTraceAt;

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
            boolean shouldLogSimulationTrace = false;
            SimulationTraceSnapshot traceSnapshot = null;

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
                    long traceNow = System.currentTimeMillis();
                    shouldLogSimulationTrace = SIM_PACKET_TRACE
                            && traceNow - lastSimPacketTraceAt >= SIM_PACKET_TRACE_COOLDOWN_MS;
                    if (shouldLogSimulationTrace) {
                        traceSnapshot = captureSimulationTraceSnapshot(offset, flagId, traceNow);
                    }
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
            if (shouldLogSimulationTrace && traceSnapshot != null) {
                maybeLogSimulationPacketTrace(traceSnapshot);
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

    private void maybeLogSimulationPacketTrace(SimulationTraceSnapshot snapshot) {
        if (!SIM_PACKET_TRACE) {
            return;
        }
        if (snapshot.capturedAtMs() - lastSimPacketTraceAt < SIM_PACKET_TRACE_COOLDOWN_MS) {
            return;
        }
        lastSimPacketTraceAt = snapshot.capturedAtMs();

        LogUtil.warn(String.format(
                "[SimulationTrace] subject=%s version=%s protocol=%d offset=%.6f gl=%d " +
                        "pkt=%s ageMs=%d hasPos=%s hasRot=%s onGround=%s hCollision=%s teleportAccept=%s " +
                        "move=%s " +
                        "statePos=%s claimedPos=%s stateOnGround=%s claimedOnGround=%s " +
                        "supportPos=%s supportOnGround=%s feetBlock=%s headBlock=%s " +
                        "softH=%s hardH=%s vertCol=%s step=%s slimeStep=%s nearFluid=%s nearGlitch=%s ogUncertain=%s",
                snapshot.subject(),
                snapshot.version(),
                snapshot.protocol(),
                snapshot.offset(),
                snapshot.flagId(),
                snapshot.packetType(),
                snapshot.movementPacketAgeMs(),
                snapshot.movementHadPosition(),
                snapshot.movementHadRotation(),
                snapshot.movementOnGround(),
                snapshot.movementHorizontalCollision(),
                snapshot.movementWasTeleportAccept(),
                snapshot.movementSummary(),
                snapshot.statePosition(),
                snapshot.claimedPosition(),
                snapshot.stateOnGround(),
                snapshot.claimedOnGround(),
                snapshot.supportPosition(),
                snapshot.supportOnGround(),
                snapshot.feetBlock(),
                snapshot.headBlock(),
                snapshot.softHorizontalCollision(),
                snapshot.horizontalCollision(),
                snapshot.verticalCollision(),
                snapshot.stepMovement(),
                snapshot.steppingOnSlime(),
                snapshot.nearFluid(),
                snapshot.nearGlitchyBlock(),
                snapshot.onGroundUncertain()
        ));
    }

    private SimulationTraceSnapshot captureSimulationTraceSnapshot(double offset, int flagId, long capturedAtMs) {
        boolean sensitive = SIM_PACKET_TRACE_SENSITIVE;
        return new SimulationTraceSnapshot(
                capturedAtMs,
                buildSubjectLabel(player.getName(), String.valueOf(player.user.getUUID()), sensitive),
                player.getClientVersion().getReleaseName(),
                player.getClientVersion().getProtocolVersion(),
                offset,
                flagId,
                player.packetStateData.lastMovementPacketType,
                Math.max(0L, capturedAtMs - player.packetStateData.lastMovementPacketAtMs),
                player.packetStateData.lastMovementHadPosition,
                player.packetStateData.lastMovementHadRotation,
                player.packetStateData.lastMovementOnGround,
                player.packetStateData.lastMovementHorizontalCollision,
                player.packetStateData.lastMovementWasTeleportAccept,
                formatMovement(player.packetStateData.lastMovementX, player.packetStateData.lastMovementY, player.packetStateData.lastMovementZ, player.packetStateData.lastMovementYaw, player.packetStateData.lastMovementPitch, sensitive),
                formatCoords(player.x, player.y, player.z, sensitive),
                formatCoords(
                        player.packetStateData.lastClaimedPosition.getX(),
                        player.packetStateData.lastClaimedPosition.getY(),
                        player.packetStateData.lastClaimedPosition.getZ(),
                        sensitive
                ),
                player.onGround,
                player.packetStateData.packetPlayerOnGround,
                formatSupportPos(player.mainSupportingBlockData.blockPos(), sensitive),
                player.mainSupportingBlockData.onGround(),
                describeBlockAt(player, player.x, player.y - 0.01, player.z, sensitive),
                describeBlockAt(player, player.x, player.y + player.getEyeHeight(), player.z, sensitive),
                player.softHorizontalCollision,
                player.horizontalCollision,
                player.verticalCollision,
                player.uncertaintyHandler.isStepMovement,
                player.uncertaintyHandler.isSteppingOnSlime,
                player.pointThreeEstimator.isNearFluid,
                player.uncertaintyHandler.isNearGlitchyBlock,
                player.uncertaintyHandler.onGroundUncertain
        );
    }

    private static String formatSupportPos(Vector3i pos, boolean sensitive) {
        if (pos == null) {
            return "null";
        }
        if (!sensitive) {
            return "chunk=" + (pos.getX() >> 4) + "," + (pos.getY() >> 4) + "," + (pos.getZ() >> 4);
        }
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static String describeBlockAt(GrimPlayer player, double x, double y, double z, boolean sensitive) {
        WrappedBlockState state = player.compensatedWorld.getBlock(x, y, z);
        if (state == null) {
            return "null";
        }
        if (!sensitive) {
            return String.valueOf(state.getType());
        }
        return state.getType() + "#" + state.getGlobalId();
    }

    private static String formatCoords(double x, double y, double z, boolean sensitive) {
        if (sensitive) {
            return String.format(Locale.ROOT, "(%.3f,%.3f,%.3f)", x, y, z);
        }
        return "chunk=(" + floorDiv16(x) + "," + floorDiv16(y) + "," + floorDiv16(z) + ")";
    }

    private static String formatMovement(double x, double y, double z, float yaw, float pitch, boolean sensitive) {
        if (sensitive) {
            return String.format(Locale.ROOT, "(%.3f,%.3f,%.3f yaw=%.2f pitch=%.2f)", x, y, z, yaw, pitch);
        }
        int yawBucket = Math.floorMod((int) Math.floor(yaw), 360) / 45;
        int pitchBucket = Math.max(-2, Math.min(2, (int) Math.floor(pitch / 45.0F)));
        return String.format(Locale.ROOT, "chunk=(%d,%d,%d) yawOctant=%d pitchBand=%d",
                floorDiv16(x), floorDiv16(y), floorDiv16(z), yawBucket, pitchBucket);
    }

    private static int floorDiv16(double value) {
        return (int) Math.floor(value / 16.0D);
    }

    private static String buildSubjectLabel(String playerName, String uuid, boolean sensitive) {
        if (sensitive) {
            return playerName + "/" + uuid;
        }
        return "anon#" + Integer.toUnsignedString((playerName + "|" + uuid).hashCode(), 36);
    }

    private record SimulationTraceSnapshot(
            long capturedAtMs,
            String subject,
            String version,
            int protocol,
            double offset,
            int flagId,
            String packetType,
            long movementPacketAgeMs,
            boolean movementHadPosition,
            boolean movementHadRotation,
            boolean movementOnGround,
            boolean movementHorizontalCollision,
            boolean movementWasTeleportAccept,
            String movementSummary,
            String statePosition,
            String claimedPosition,
            boolean stateOnGround,
            boolean claimedOnGround,
            String supportPosition,
            boolean supportOnGround,
            String feetBlock,
            String headBlock,
            boolean softHorizontalCollision,
            boolean horizontalCollision,
            boolean verticalCollision,
            boolean stepMovement,
            boolean steppingOnSlime,
            boolean nearFluid,
            boolean nearGlitchyBlock,
            boolean onGroundUncertain
    ) {}
}
