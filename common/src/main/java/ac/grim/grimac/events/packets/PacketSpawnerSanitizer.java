package ac.grim.grimac.events.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTShort;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;

public class PacketSpawnerSanitizer extends PacketListenerAbstract {
    private static final short SANITIZED_SPAWNER_DELAY = 20;
    private static final ClientVersion SERVER_CLIENT_VERSION =
            PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();
    private static final int MOB_SPAWNER_TYPE_ID = BlockEntityTypes.MOB_SPAWNER.getId(SERVER_CLIENT_VERSION);

    public PacketSpawnerSanitizer() {
        // Run after world cache listeners to avoid affecting Grim internals.
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.BLOCK_ENTITY_DATA) {
            sanitizeBlockEntityData(event);
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            sanitizeChunkData(event);
        }
    }

    private void sanitizeBlockEntityData(PacketSendEvent event) {
        try {
            WrapperPlayServerBlockEntityData wrapper = new WrapperPlayServerBlockEntityData(event);
            if (wrapper.getBlockEntityType() != BlockEntityTypes.MOB_SPAWNER) {
                return;
            }
            if (sanitizeSpawnerNbt(wrapper.getNBT())) {
                event.markForReEncode(true);
            }
        } catch (Exception ex) {
            PacketDecodeUtils.logSuppressedDecode("PacketSpawnerSanitizer(BLOCK_ENTITY_DATA)", event.getPacketType(), ex);
        }
    }

    private void sanitizeChunkData(PacketSendEvent event) {
        try {
            WrapperPlayServerChunkData wrapper = new WrapperPlayServerChunkData(event);
            boolean changed = false;
            for (TileEntity tileEntity : wrapper.getColumn().getTileEntities()) {
                if (tileEntity.getType() != MOB_SPAWNER_TYPE_ID) {
                    continue;
                }
                changed |= sanitizeSpawnerNbt(tileEntity.getNBT());
            }
            if (changed) {
                event.markForReEncode(true);
            }
        } catch (Exception ex) {
            PacketDecodeUtils.logSuppressedDecode("PacketSpawnerSanitizer(CHUNK_DATA)", event.getPacketType(), ex);
        }
    }

    private boolean sanitizeSpawnerNbt(NBTCompound nbt) {
        if (nbt == null) {
            return false;
        }

        // Keep behavior stable while hiding activation-state hints used by clients.
        boolean changed = setDelayTag(nbt, "Delay");
        changed |= setDelayTag(nbt, "delay");
        return changed;
    }

    private boolean setDelayTag(NBTCompound nbt, String key) {
        if (nbt.getNumberTagValueOrDefault(key, SANITIZED_SPAWNER_DELAY).shortValue() == SANITIZED_SPAWNER_DELAY) {
            return false;
        }
        nbt.setTag(key, new NBTShort(SANITIZED_SPAWNER_DELAY));
        return true;
    }
}
