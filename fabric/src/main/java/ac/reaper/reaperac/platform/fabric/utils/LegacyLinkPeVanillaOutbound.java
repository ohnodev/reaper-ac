package ac.reaper.reaperac.platform.fabric.utils;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.legacylink.LegacyLinkGrimTransactionDebug;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange.EncodedBlock;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * PacketEvents encodes {@link PacketWrapper}s straight to the Netty channel, bypassing
 * {@link net.minecraft.network.Connection#send} and therefore LegacyLink's vanilla {@link Packet} rewriter.
 * For legacy (26.1) clients, re-send affected packets on {@link ServerPlayer#connection} so the wire matches
 * Mojang's codec (Grim's transaction {@link WrapperPlayServerPing} must reach the client as a real ping).
 */
public final class LegacyLinkPeVanillaOutbound {

    private LegacyLinkPeVanillaOutbound() {
    }

    /**
     * @return true if the wrapper was handled (PE must not encode/send it)
     */
    public static boolean tryRoute(Object channel, PacketWrapper<?> wrapper) {
        MinecraftServer server = GrimACFabricLoaderPlugin.FABRIC_SERVER;
        if (server == null || channel == null || wrapper == null) {
            return false;
        }
        User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        if (user == null) {
            return false;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(user.getUUID());
        if (player == null || !LegacyLinkCompat.isLegacyClient(player)) {
            return false;
        }

        if (wrapper instanceof WrapperPlayServerMultiBlockChange mbc) {
            return sendMultiBlockChange(server, player, mbc);
        }
        if (wrapper instanceof WrapperPlayServerBlockChange bc) {
            return sendBlockChange(server, player, bc);
        }
        if (wrapper instanceof WrapperPlayServerAcknowledgeBlockChanges ack) {
            sendOnServerThread(server, player, new ClientboundBlockChangedAckPacket(ack.getSequence()));
            return true;
        }
        if (wrapper instanceof WrapperPlayServerPing ping) {
            sendLegacyPingRecordingGrim(server, player, user, ping.getId());
            return true;
        }
        return false;
    }

    private static boolean sendMultiBlockChange(MinecraftServer server, ServerPlayer player, WrapperPlayServerMultiBlockChange w) {
        Vector3i cp = w.getChunkPosition();
        EncodedBlock[] blocks = w.getBlocks();
        if (cp == null || blocks == null || blocks.length == 0) {
            return false;
        }
        int chunkX = cp.getX();
        int sectionY = cp.getY();
        int chunkZ = cp.getZ();

        ServerLevel level = (ServerLevel) player.level();
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        int sectionIndex = chunk.getSectionIndexFromSectionY(sectionY);
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return false;
        }
        LevelChunkSection section = chunk.getSection(sectionIndex);

        ShortSet changes = new ShortOpenHashSet();
        for (EncodedBlock b : blocks) {
            changes.add(SectionPos.sectionRelativePos(new BlockPos(b.getX(), b.getY(), b.getZ())));
        }
        if (changes.isEmpty()) {
            return false;
        }
        SectionPos sectionPos = SectionPos.of(chunkX, sectionY, chunkZ);
        ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, changes, section);
        sendOnServerThread(server, player, packet);
        return true;
    }

    private static boolean sendBlockChange(MinecraftServer server, ServerPlayer player, WrapperPlayServerBlockChange w) {
        Vector3i p = w.getBlockPosition();
        if (p == null) {
            return false;
        }
        BlockPos pos = new BlockPos(p.getX(), p.getY(), p.getZ());
        ServerLevel level = (ServerLevel) player.level();
        sendOnServerThread(server, player, new ClientboundBlockUpdatePacket(pos, level.getBlockState(pos)));
        return true;
    }

    private static void sendOnServerThread(MinecraftServer server, ServerPlayer player, Packet<?> packet) {
        Runnable r = () -> player.connection.send(packet);
        if (server.isSameThread()) {
            r.run();
        } else {
            server.execute(r);
        }
    }

    /**
     * Vanilla ping bypasses PE's {@code PacketSendEvent} for Grim's listener. {@link PacketPingListener} normally
     * records the send during PE encode, <em>before</em> {@code ctx.write} — so {@link GrimPlayer#transactionsSent}
     * must be updated as soon as we enqueue the vanilla ping, not on flush. A fast client pong can otherwise be
     * processed while the id is still only in {@link GrimPlayer#didWeSendThatTrans}, so {@code addTransactionResponse}
     * never matches and Grim hits {@code disconnect.timeout}.
     */
    private static void sendLegacyPingRecordingGrim(
            MinecraftServer server,
            ServerPlayer player,
            User user,
            int rawId
    ) {
        if (rawId != (short) rawId) {
            sendOnServerThread(server, player, new ClientboundPingPacket(rawId));
            return;
        }
        short sid = (short) rawId;
        ClientboundPingPacket packet = new ClientboundPingPacket(rawId);
        Runnable r = () -> {
            player.connection.send(packet);
            GrimPlayer grim = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(user);
            String name = player.getGameProfile().name();
            if (grim == null) {
                LegacyLinkGrimTransactionDebug.logLegacyVanillaPingEnqueued(name, sid, null, false);
            } else {
                boolean removedPending = grim.acknowledgeVanillaPingDispatched(sid);
                LegacyLinkGrimTransactionDebug.logLegacyVanillaPingEnqueued(name, sid, grim, removedPending);
            }
        };
        if (server.isSameThread()) {
            r.run();
        } else {
            server.execute(r);
        }
    }
}
