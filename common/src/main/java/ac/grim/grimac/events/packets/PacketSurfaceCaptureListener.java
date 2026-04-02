package ac.grim.grimac.events.packets;

import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
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
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Capture-all listener for play packet surface mapping.
 *
 * Emits capture-only rows (no deep decode) for non-movement packets so we can
 * prove packet presence while keeping movement packets decoded by the movement
 * listener.
 */
public final class PacketSurfaceCaptureListener extends PacketListenerAbstract {
    private static final Gson GSON = new Gson();
    private static final Object WRITE_LOCK = new Object();
    private static final String FILE_PROP = "grim.movementCaptureFile";
    private static final String FILE_ENV = "GRIM_MOVEMENT_CAPTURE_FILE";
    private static final String DEFAULT_OUTPUT = "/tmp/grim-movement-captures.ndjson";
    private static final Pattern PACKET_KEY_EXTRACT = Pattern.compile("(Client|Server)\\.[A-Z0-9_]+");

    // Keep movement packet decode owned by PacketMovementCaptureListener.
    private static final Set<String> MOVEMENT_KEYS = Set.of(
            "Client.PLAYER_FLYING",
            "Client.PLAYER_POSITION",
            "Client.PLAYER_POSITION_AND_ROTATION",
            "Client.PLAYER_ROTATION",
            "Client.VEHICLE_MOVE",
            "Client.CLIENT_TICK_END",
            "Client.PLAYER_INPUT",
            "Server.PLAYER_POSITION_AND_LOOK",
            "Server.ENTITY_POSITION_SYNC",
            "Server.VEHICLE_MOVE"
    );

    private final Path outputPath;
    private BufferedWriter writer;

    public PacketSurfaceCaptureListener() {
        super(PacketListenerPriority.MONITOR);
        this.outputPath = Paths.get(resolveOutputPath()).toAbsolutePath().normalize();
        LogUtil.info("Packet surface capture enabled. Writing NDJSON to " + this.outputPath);
    }

    private static String resolveOutputPath() {
        String fromProp = System.getProperty(FILE_PROP);
        if (fromProp != null && !fromProp.isBlank()) return fromProp;
        String fromEnv = System.getenv(FILE_ENV);
        if (fromEnv != null && !fromEnv.isBlank()) return fromEnv;
        return DEFAULT_OUTPUT;
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

    private String extractPacketKey(Object packetType) {
        if (packetType instanceof PacketTypeCommon common) {
            String side = common.getSide() == PacketSide.CLIENT ? "Client" : "Server";
            return side + "." + common.getName();
        }
        if (packetType == null) return null;
        String raw = packetType.toString();
        var m = PACKET_KEY_EXTRACT.matcher(raw);
        if (!m.find()) return null;
        return m.group();
    }

    private void emitCaptureOnly(String packetKey) {
        JsonObject decode773 = new JsonObject();
        JsonObject decode775 = new JsonObject();
        decode773.addProperty("_capture_only", true);
        decode775.addProperty("_capture_only", true);

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
                LogUtil.error("Failed writing packet surface capture row: " + ex.getMessage());
            }
        }
    }

    private void capture(Object packetType) {
        String packetKey = extractPacketKey(packetType);
        if (packetKey == null) return;
        if (MOVEMENT_KEYS.contains(packetKey)) return;
        emitCaptureOnly(packetKey);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        capture(event.getPacketType());
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        capture(event.getPacketType());
    }
}
