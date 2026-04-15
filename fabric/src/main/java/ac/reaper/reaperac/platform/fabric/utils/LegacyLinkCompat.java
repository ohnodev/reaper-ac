package ac.reaper.reaperac.platform.fabric.utils;

import ac.reaper.reaperac.utils.anticheat.LogUtil;
import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.HandlerNames;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bridges Reaper's direct PacketEvents sends with LegacyLink's block-state remapper for legacy clients.
 */
public final class LegacyLinkCompat {
    private static final AtomicBoolean loggedFailure = new AtomicBoolean(false);
    private static final AtomicBoolean loggedRemapFailure = new AtomicBoolean(false);
    private static final AtomicBoolean loggedChannelFailure = new AtomicBoolean(false);
    private static final AtomicInteger traceBudget = new AtomicInteger(64);
    private static final boolean TRACE_LEGACY_RESYNC =
            Boolean.parseBoolean(System.getProperty("reaper.traceLegacyResync", "false"));

    private static final Method LEGACY_TRACKER_IS_LEGACY;
    private static final Method REGISTRY_REMAPPER_REMAP_BLOCK_STATE;
    private static final Field SERVER_PACKET_LISTENER_CONNECTION;

    static {
        Method trackerMethod = null;
        Method remapMethod = null;
        Field connectionField = null;
        ReflectiveOperationException initFailure = null;
        try {
            Class<?> trackerClass = Class.forName("dev.ohno.legacylink.connection.LegacyTracker");
            trackerMethod = trackerClass.getMethod("isLegacy", Connection.class);
            Class<?> remapperClass = Class.forName("dev.ohno.legacylink.mapping.RegistryRemapper");
            remapMethod = remapperClass.getMethod("remapBlockState", int.class);
            connectionField = findConnectionField(ServerGamePacketListenerImpl.class);
            if (connectionField == null) {
                throw new NoSuchFieldException(
                        "No field of type Connection on ServerGamePacketListenerImpl (intermediary/runtime names differ from Yarn)");
            }
        } catch (ReflectiveOperationException e) {
            initFailure = e;
        }
        LEGACY_TRACKER_IS_LEGACY = trackerMethod;
        REGISTRY_REMAPPER_REMAP_BLOCK_STATE = remapMethod;
        SERVER_PACKET_LISTENER_CONNECTION = connectionField;
        if (initFailure != null) {
            LogUtil.warn(
                    "LegacyLinkCompat static init failed; Reaper resync will not remap block states for legacy clients: "
                            + initFailure
            );
        }
    }

    /**
     * Yarn names fields in sources, but the server loads Minecraft with intermediary field names.
     * Resolve the listener's {@link Connection} field by declared type instead of by name.
     */
    private static Field findConnectionField(Class<?> listenerClass) {
        for (Class<?> c = listenerClass; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == Connection.class) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        return null;
    }

    private LegacyLinkCompat() {
    }

    /**
     * True when this player's connection is marked legacy by LegacyLink (26.1.x client on 26.2 server).
     */
    public static boolean isLegacyClient(ServerPlayer player) {
        if (player == null
                || LEGACY_TRACKER_IS_LEGACY == null
                || SERVER_PACKET_LISTENER_CONNECTION == null) {
            return false;
        }
        try {
            Connection connection = (Connection) SERVER_PACKET_LISTENER_CONNECTION.get(player.connection);
            return connection != null && (boolean) LEGACY_TRACKER_IS_LEGACY.invoke(null, connection);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (loggedFailure.compareAndSet(false, true)) {
                LogUtil.error("Failed to read LegacyLink legacy flag for player connection.", ex);
            }
            return false;
        }
    }

    /**
     * True when this Netty channel's {@link Connection} is marked legacy by LegacyLink.
     * Used before the player entity exists (e.g. PacketEvents decode).
     */
    public static boolean isLegacyNettyChannel(Object channelObj) {
        if (LEGACY_TRACKER_IS_LEGACY == null || !(channelObj instanceof Channel ch)) {
            return false;
        }
        try {
            Object h = ch.pipeline().get(HandlerNames.PACKET_HANDLER);
            if (!(h instanceof Connection connection)) {
                return false;
            }
            return (boolean) LEGACY_TRACKER_IS_LEGACY.invoke(null, connection);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (loggedChannelFailure.compareAndSet(false, true)) {
                LogUtil.error("Failed to read LegacyLink legacy flag from Netty channel.", ex);
            }
            return false;
        }
    }

    public static int mapBlockStateIdForClient(ServerPlayer player, int translatedBlockStateId) {
        if (player == null) {
            return translatedBlockStateId;
        }
        if (REGISTRY_REMAPPER_REMAP_BLOCK_STATE == null || !isLegacyClient(player)) {
            return translatedBlockStateId;
        }

        try {
            int remapped = (int) REGISTRY_REMAPPER_REMAP_BLOCK_STATE.invoke(null, translatedBlockStateId);
            // TRACE_LEGACY_RESYNC behavior:
            // - Always log true remaps (remapped != translatedBlockStateId) without consuming traceBudget.
            // - Also log the first N non-remap calls while traceBudget > 0 to sample baseline traffic.
            if (TRACE_LEGACY_RESYNC && (remapped != translatedBlockStateId || traceBudget.getAndDecrement() > 0)) {
                LogUtil.info(
                        "[ReaperLegacyBridge] player="
                                + player.getName().getString()
                                + " state="
                                + translatedBlockStateId
                                + "->"
                                + remapped
                                + " changed="
                                + (remapped != translatedBlockStateId)
                );
            }
            return remapped;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (loggedRemapFailure.compareAndSet(false, true)) {
                LogUtil.error("Failed to apply LegacyLink block-state remap bridge in Reaper resync path.", ex);
            }
            return translatedBlockStateId;
        }
    }
}
