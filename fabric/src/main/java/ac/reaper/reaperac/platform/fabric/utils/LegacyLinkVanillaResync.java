package ac.reaper.reaperac.platform.fabric.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

/**
 * Sends block resync packets through {@link net.minecraft.network.Connection#send} so LegacyLink's
 * {@code Connection.send} mixins run. PacketEvents {@code User#sendPacket} bypasses that pipeline and
 * was crashing 26.1 clients (e.g. leaf_litter) with raw 26.2 global state ids.
 */
public final class LegacyLinkVanillaResync {

    private LegacyLinkVanillaResync() {
    }

    public static boolean trySendSectionBlocksUpdate(
            ServerPlayer player,
            int chunkX,
            int sectionY,
            int chunkZ,
            int minLocalX,
            int maxLocalX,
            int minLocalY,
            int maxLocalY,
            int minLocalZ,
            int maxLocalZ
    ) {
        if (!LegacyLinkCompat.isLegacyClient(player)) {
            return false;
        }
        ServerLevel level = (ServerLevel) player.level();
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        int sectionIndex = chunk.getSectionIndexFromSectionY(sectionY);
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            return false;
        }
        LevelChunkSection section = chunk.getSection(sectionIndex);
        ShortSet changes = new ShortOpenHashSet();
        for (int lz = minLocalZ; lz <= maxLocalZ; lz++) {
            for (int lx = minLocalX; lx <= maxLocalX; lx++) {
                for (int ly = minLocalY; ly <= maxLocalY; ly++) {
                    int worldX = chunkX * 16 + lx;
                    int worldY = SectionPos.sectionToBlockCoord(sectionY) + ly;
                    int worldZ = chunkZ * 16 + lz;
                    changes.add(SectionPos.sectionRelativePos(new BlockPos(worldX, worldY, worldZ)));
                }
            }
        }
        if (changes.isEmpty()) {
            return false;
        }
        SectionPos sectionPos = SectionPos.of(chunkX, sectionY, chunkZ);
        ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, changes, section);
        player.connection.send(packet);
        return true;
    }

    public static boolean trySendSingleBlockUpdate(ServerPlayer player, int x, int y, int z, int sequence) {
        if (!LegacyLinkCompat.isLegacyClient(player)) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = ((ServerLevel) player.level()).getBlockState(pos);
        player.connection.send(new ClientboundBlockUpdatePacket(pos, state));
        player.connection.send(new ClientboundBlockChangedAckPacket(sequence));
        return true;
    }
}
