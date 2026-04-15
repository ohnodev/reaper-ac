package ac.reaper.reaperac.checks.impl.prediction;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.api.config.ConfigManager;
import ac.reaper.reaperac.api.event.events.CompletePredictionEvent;
import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.platform.api.world.PlatformChunk;
import ac.reaper.reaperac.platform.api.world.PlatformWorld;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import ac.reaper.reaperac.utils.math.Vector3dm;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@CheckData(name = "Simulation", decay = 0.02)
public class OffsetHandler extends Check implements PostPredictionCheck {
    private static final AtomicInteger flags = new AtomicInteger(0);
    private static final boolean SIM_PACKET_TRACE = Boolean.getBoolean("grim.simulationPacketTrace");
    /** Reaper: enable without {@code GRIM_SIM_TRACE}; logs {@code [SimulationTrace]} on Simulation flags. */
    private static final boolean REAPER_SIM_TRACE =
            Boolean.parseBoolean(System.getProperty("reaper.simulationTrace", "false"));
    private static final boolean SIM_PACKET_TRACE_SENSITIVE = Boolean.getBoolean("grim.simulationPacketTraceSensitive");
    private static final boolean REAPER_SIM_TRACE_SENSITIVE =
            Boolean.parseBoolean(System.getProperty("reaper.simulationTraceSensitive", "false"));
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

    /**
     * Hard movement-state transitions (respawn/teleport/gamemode swap) invalidate prior
     * simulation debt. Resetting here prevents stale violations from repeatedly setbacking
     * otherwise-valid movement after a resync boundary.
     */
    public void resetSimulationState() {
        advantageGained = 0;
        violations = 0;
        removeOffsetLenience();
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
                    shouldLogSimulationTrace = (SIM_PACKET_TRACE || REAPER_SIM_TRACE)
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
        if (!SIM_PACKET_TRACE && !REAPER_SIM_TRACE) {
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
                        "supportPos=%s supportOnGround=%s supportBlock=%s feetBlock=%s headBlock=%s " +
                        "softH=%s hardH=%s vertCol=%s step=%s slimeStep=%s nearFluid=%s nearFluidSrc=%s nearGlitch=%s ogUncertain=%s " +
                        "stuck=%s friction=%s cVel=%s predIn=%s",
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
                snapshot.supportBlock(),
                snapshot.feetBlock(),
                snapshot.headBlock(),
                snapshot.softHorizontalCollision(),
                snapshot.horizontalCollision(),
                snapshot.verticalCollision(),
                snapshot.stepMovement(),
                snapshot.steppingOnSlime(),
                snapshot.nearFluid(),
                snapshot.nearFluidSource(),
                snapshot.nearGlitchyBlock(),
                snapshot.onGroundUncertain(),
                snapshot.stuckSpeedMultiplier(),
                snapshot.friction(),
                snapshot.clientVelocity(),
                snapshot.predictedInput()
        ));
    }

    private SimulationTraceSnapshot captureSimulationTraceSnapshot(double offset, int flagId, long capturedAtMs) {
        boolean sensitive = SIM_PACKET_TRACE_SENSITIVE || REAPER_SIM_TRACE_SENSITIVE;
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
                describeBlockAt(player, player.mainSupportingBlockData.blockPos(), sensitive),
                describeBlockAt(player, player.x, player.y - 0.01, player.z, sensitive),
                describeBlockAt(player, player.x, player.y + player.getEyeHeight(), player.z, sensitive),
                player.softHorizontalCollision,
                player.horizontalCollision,
                player.verticalCollision,
                player.uncertaintyHandler.isStepMovement,
                player.uncertaintyHandler.isSteppingOnSlime,
                player.pointThreeEstimator.isNearFluid,
                player.pointThreeEstimator.nearFluidSource,
                player.uncertaintyHandler.isNearGlitchyBlock,
                player.uncertaintyHandler.onGroundUncertain,
                formatVector(player.stuckSpeedMultiplier, sensitive),
                String.format(Locale.ROOT, "%.5f", player.friction),
                formatVector(player.clientVelocity, sensitive),
                formatVector(player.predictedVelocity == null ? null : player.predictedVelocity.input, sensitive)
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
        int bx = (int) Math.floor(x);
        int by = (int) Math.floor(y);
        int bz = (int) Math.floor(z);
        WrappedBlockState state = player.compensatedWorld.getBlock(x, y, z);
        return describeState(state, sensitive) + describeNativeState(player, bx, by, bz, sensitive);
    }

    private static String describeBlockAt(GrimPlayer player, Vector3i pos, boolean sensitive) {
        if (pos == null) {
            return "null";
        }
        WrappedBlockState state = player.compensatedWorld.getBlock(pos.getX(), pos.getY(), pos.getZ());
        return describeState(state, sensitive) + describeNativeState(player, pos.getX(), pos.getY(), pos.getZ(), sensitive);
    }

    private static String describeNativeState(GrimPlayer player, int x, int y, int z, boolean sensitive) {
        if (!sensitive || player.platformPlayer == null) {
            return "";
        }
        try {
            PlatformWorld world = player.platformPlayer.getWorld();
            if (world == null || !world.isLoaded()) {
                return "{native=unloaded}";
            }
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                return "{native=chunk_unloaded}";
            }
            PlatformChunk chunk = world.getChunkAt(chunkX, chunkZ);
            if (chunk == null) {
                return "{native=chunk_null}";
            }
            int nativeId = chunk.getBlockID(x & 15, y, z & 15);
            String nativeState = chunk.getBlockStateString(x & 15, y, z & 15);
            return "{native=" + nativeState + "#" + nativeId + "}";
        } catch (Throwable throwable) {
            return "{native=error:" + throwable.getClass().getSimpleName() + "}";
        }
    }

    private static String describeState(WrappedBlockState state, boolean sensitive) {
        if (state == null) {
            return "null";
        }
        boolean waterlogged = state.hasProperty(StateValue.WATERLOGGED) && state.isWaterlogged();
        String amountSuffix = "";
        if (state.getType() == StateTypes.LEAF_LITTER) {
            amountSuffix = "[seg=" + state.getSegmentAmount() + "]";
        } else if (state.getType() == StateTypes.WILDFLOWERS) {
            amountSuffix = "[flowers=" + state.getFlowerAmount() + "]";
        }
        if (!sensitive) {
            return state.getType() + amountSuffix + (waterlogged ? "[wl]" : "");
        }
        return state.getType() + "#" + state.getGlobalId() + amountSuffix + (waterlogged ? "[wl]" : "");
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

    private static String formatVector(Vector3dm vector, boolean sensitive) {
        if (vector == null) {
            return "null";
        }
        if (sensitive) {
            return String.format(Locale.ROOT, "(%.5f,%.5f,%.5f)", vector.getX(), vector.getY(), vector.getZ());
        }
        return String.format(Locale.ROOT, "(%.3f,%.3f,%.3f)", vector.getX(), vector.getY(), vector.getZ());
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
            String supportBlock,
            String feetBlock,
            String headBlock,
            boolean softHorizontalCollision,
            boolean horizontalCollision,
            boolean verticalCollision,
            boolean stepMovement,
            boolean steppingOnSlime,
            boolean nearFluid,
            String nearFluidSource,
            boolean nearGlitchyBlock,
            boolean onGroundUncertain,
            String stuckSpeedMultiplier,
            String friction,
            String clientVelocity,
            String predictedInput
    ) {}
}
