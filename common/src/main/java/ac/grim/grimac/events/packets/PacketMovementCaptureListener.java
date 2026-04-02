package ac.grim.grimac.events.packets;

import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientVehicleMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerVehicleMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Packet capture listener for migration testing.
 *
 * Emits NDJSON rows in the format consumed by tests/scripts/compare-movement-captures.py:
 * {
 *   "packet": "Client.PLAYER_POSITION",
 *   "phase": "play",
 *   "decode_773": { ... },
 *   "decode_775": { ... },
 *   "ts": "..."
 * }
 */
public final class PacketMovementCaptureListener extends PacketListenerAbstract {
    private static final Gson GSON = new Gson();
    private static final Object WRITE_LOCK = new Object();
    private static final String ENABLE_ENV = "GRIM_MOVEMENT_CAPTURE";
    private static final String FILE_ENV = "GRIM_MOVEMENT_CAPTURE_FILE";
    private static final String ENABLE_PROP = "grim.movementCapture";
    private static final String FILE_PROP = "grim.movementCaptureFile";
    private static final String DEFAULT_OUTPUT = "/tmp/grim-movement-captures.ndjson";
    private static final Pattern PACKET_KEY_EXTRACT = Pattern.compile("(Client|Server)\\.[A-Z0-9_]+");
    private static final Pattern SIMPLE_PACKET_KEY = Pattern.compile("^(Client|Server)\\.[A-Z0-9_]+$");

    private final Path outputPath;
    private BufferedWriter writer;

    private static boolean isTruthy(String value) {
        if (value == null) return false;
        String normalized = value.trim().toLowerCase();
        return normalized.equals("1")
                || normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("on");
    }

    public static boolean isEnabled() {
        return isTruthy(System.getProperty(ENABLE_PROP)) || isTruthy(System.getenv(ENABLE_ENV));
    }

    private static String resolveOutputPath() {
        String fromProp = System.getProperty(FILE_PROP);
        if (fromProp != null && !fromProp.isBlank()) return fromProp;
        String fromEnv = System.getenv(FILE_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
        return DEFAULT_OUTPUT;
    }

    public PacketMovementCaptureListener() {
        super(PacketListenerPriority.MONITOR);
        this.outputPath = Paths.get(resolveOutputPath()).toAbsolutePath().normalize();
        LogUtil.info("Movement capture enabled. Writing NDJSON to " + this.outputPath);
    }

    private BufferedWriter openWriterIfNeeded() throws IOException {
        if (writer != null) return writer;
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        return writer;
    }

    private void emit(String packetKey, JsonObject decode773, JsonObject decode775) {
        JsonObject row = new JsonObject();
        row.addProperty("packet", packetKey);
        row.addProperty("phase", "play");
        row.add("decode_773", decode773);
        row.add("decode_775", decode775);
        row.addProperty("ts", Instant.now().toString());

        synchronized (WRITE_LOCK) {
            try {
                BufferedWriter bw = openWriterIfNeeded();
                bw.write(GSON.toJson(row));
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                LogUtil.error("Failed writing movement capture row: " + ex.getMessage());
            }
        }
    }

    private void emitCaptureOnly(String packetKey) {
        JsonObject decode773 = new JsonObject();
        JsonObject decode775 = new JsonObject();
        decode773.addProperty("_capture_only", true);
        decode775.addProperty("_capture_only", true);
        emit(packetKey, decode773, decode775);
    }

    private String extractPacketKey(Object packetType) {
        if (packetType instanceof PacketTypeCommon common) {
            String side = common.getSide() == PacketSide.CLIENT ? "Client" : "Server";
            return side + "." + common.getName();
        }
        if (packetType == null) return null;
        String raw = packetType.toString();
        var m = PACKET_KEY_EXTRACT.matcher(raw);
        if (!m.find()) return null;
        String key = m.group();
        return SIMPLE_PACKET_KEY.matcher(key).matches() ? key : null;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            String packetKey;
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();

            decode775.addProperty("has_position", packet.hasPositionChanged());
            decode775.addProperty("has_rotation", packet.hasRotationChanged());
            decode775.addProperty("on_ground", packet.isOnGround());
            decode775.addProperty("horizontal_collision", packet.isHorizontalCollision());

            if (packet.hasPositionChanged()) {
                decode775.addProperty("x", packet.getLocation().getX());
                decode775.addProperty("y", packet.getLocation().getY());
                decode775.addProperty("z", packet.getLocation().getZ());
            }
            if (packet.hasRotationChanged()) {
                decode775.addProperty("yaw", packet.getLocation().getYaw());
                decode775.addProperty("pitch", packet.getLocation().getPitch());
            }

            if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
                packetKey = "Client.PLAYER_POSITION";
                decode773.addProperty("x", packet.getLocation().getX());
                decode773.addProperty("y", packet.getLocation().getY());
                decode773.addProperty("z", packet.getLocation().getZ());
                decode773.addProperty("on_ground", packet.isOnGround());
            } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
                packetKey = "Client.PLAYER_POSITION_AND_ROTATION";
                decode773.addProperty("x", packet.getLocation().getX());
                decode773.addProperty("y", packet.getLocation().getY());
                decode773.addProperty("z", packet.getLocation().getZ());
                decode773.addProperty("yaw", packet.getLocation().getYaw());
                decode773.addProperty("pitch", packet.getLocation().getPitch());
                decode773.addProperty("on_ground", packet.isOnGround());
            } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
                packetKey = "Client.PLAYER_ROTATION";
                decode773.addProperty("yaw", packet.getLocation().getYaw());
                decode773.addProperty("pitch", packet.getLocation().getPitch());
                decode773.addProperty("on_ground", packet.isOnGround());
            } else {
                packetKey = "Client.PLAYER_FLYING";
                decode773.addProperty("on_ground", packet.isOnGround());
            }

            emit(packetKey, decode773, decode775);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE) {
            WrapperPlayClientVehicleMove packet = new WrapperPlayClientVehicleMove(event);
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();
            Vector3d pos = packet.getPosition();

            decode773.addProperty("x", pos.getX());
            decode773.addProperty("y", pos.getY());
            decode773.addProperty("z", pos.getZ());
            decode773.addProperty("yaw", packet.getYaw());
            decode773.addProperty("pitch", packet.getPitch());
            decode773.addProperty("on_ground", packet.isOnGround());

            decode775.addProperty("x", pos.getX());
            decode775.addProperty("y", pos.getY());
            decode775.addProperty("z", pos.getZ());
            decode775.addProperty("yaw", packet.getYaw());
            decode775.addProperty("pitch", packet.getPitch());
            decode775.addProperty("on_ground", packet.isOnGround());

            emit("Client.VEHICLE_MOVE", decode773, decode775);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLIENT_TICK_END) {
            emit("Client.CLIENT_TICK_END", new JsonObject(), new JsonObject());
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT) {
            WrapperPlayClientPlayerInput packet = new WrapperPlayClientPlayerInput(event);
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();

            decode773.addProperty("forward", packet.isForward());
            decode773.addProperty("backward", packet.isBackward());
            decode773.addProperty("left", packet.isLeft());
            decode773.addProperty("right", packet.isRight());
            decode773.addProperty("jump", packet.isJump());
            decode773.addProperty("shift", packet.isShift());
            decode773.addProperty("sprint", packet.isSprint());

            decode775.addProperty("forward", packet.isForward());
            decode775.addProperty("backward", packet.isBackward());
            decode775.addProperty("left", packet.isLeft());
            decode775.addProperty("right", packet.isRight());
            decode775.addProperty("jump", packet.isJump());
            decode775.addProperty("shift", packet.isShift());
            decode775.addProperty("sprint", packet.isSprint());

            emit("Client.PLAYER_INPUT", decode773, decode775);
        }

        // Fallback capture path for all other play-side client packets.
        String packetKey = extractPacketKey(event.getPacketType());
        if (packetKey != null) {
            emitCaptureOnly(packetKey);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(event);
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();

            decode773.addProperty("x", packet.getX());
            decode773.addProperty("y", packet.getY());
            decode773.addProperty("z", packet.getZ());
            decode773.addProperty("yaw", packet.getYaw());
            decode773.addProperty("pitch", packet.getPitch());
            decode773.addProperty("flags_mask", packet.getRelativeFlags().getMask());
            decode773.addProperty("teleport_id", packet.getTeleportId());

            decode775.addProperty("x", packet.getX());
            decode775.addProperty("y", packet.getY());
            decode775.addProperty("z", packet.getZ());
            decode775.addProperty("yaw", packet.getYaw());
            decode775.addProperty("pitch", packet.getPitch());
            decode775.addProperty("flags_mask", packet.getRelativeFlags().getMask());
            decode775.addProperty("teleport_id", packet.getTeleportId());
            decode775.addProperty("dismount_vehicle", packet.isDismountVehicle());
            Vector3d delta = packet.getDeltaMovement();
            decode775.addProperty("delta_x", delta.getX());
            decode775.addProperty("delta_y", delta.getY());
            decode775.addProperty("delta_z", delta.getZ());

            emit("Server.PLAYER_POSITION_AND_LOOK", decode773, decode775);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_POSITION_SYNC) {
            WrapperPlayServerEntityPositionSync packet = new WrapperPlayServerEntityPositionSync(event);
            EntityPositionData values = packet.getValues();
            Vector3d pos = values.getPosition();
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();

            decode773.addProperty("entity_id", packet.getId());
            decode773.addProperty("x", pos.getX());
            decode773.addProperty("y", pos.getY());
            decode773.addProperty("z", pos.getZ());
            decode773.addProperty("yaw", values.getYaw());
            decode773.addProperty("pitch", values.getPitch());

            decode775.addProperty("entity_id", packet.getId());
            decode775.addProperty("x", pos.getX());
            decode775.addProperty("y", pos.getY());
            decode775.addProperty("z", pos.getZ());
            decode775.addProperty("yaw", values.getYaw());
            decode775.addProperty("pitch", values.getPitch());

            emit("Server.ENTITY_POSITION_SYNC", decode773, decode775);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.VEHICLE_MOVE) {
            WrapperPlayServerVehicleMove packet = new WrapperPlayServerVehicleMove(event);
            Vector3d pos = packet.getPosition();
            JsonObject decode773 = new JsonObject();
            JsonObject decode775 = new JsonObject();

            decode773.addProperty("x", pos.getX());
            decode773.addProperty("y", pos.getY());
            decode773.addProperty("z", pos.getZ());

            decode775.addProperty("x", pos.getX());
            decode775.addProperty("y", pos.getY());
            decode775.addProperty("z", pos.getZ());

            emit("Server.VEHICLE_MOVE", decode773, decode775);
            return;
        }

        // Fallback capture path for all other play-side server packets.
        String packetKey = extractPacketKey(event.getPacketType());
        if (packetKey != null) {
            emitCaptureOnly(packetKey);
        }
    }
}
