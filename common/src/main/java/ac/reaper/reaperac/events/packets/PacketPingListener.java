package ac.reaper.reaperac.events.packets;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.Pair;
import ac.reaper.reaperac.utils.legacylink.LegacyLinkGrimTransactionDebug;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;

public class PacketPingListener extends PacketListenerAbstract {

    // Must listen on LOWEST (or maybe low) to stop Tuinity packet limiter from kicking players for transaction/pong spam
    public PacketPingListener() {
        super(PacketListenerPriority.LOWEST);
    }


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastTransactionPacketWasValid = false;
            try {
                WrapperPlayClientWindowConfirmation transaction = new WrapperPlayClientWindowConfirmation(event);
                short id = transaction.getActionId();

                // Vanilla always uses an ID starting from 1
                // Check if we sent this packet before cancelling it
                if (id <= 0 && player.addTransactionResponse(id)) {
                    player.packetStateData.lastTransactionPacketWasValid = true;
                    event.setCancelled(true);
                }
            } catch (Exception e) {
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PONG) {
            handlePlayOrConfigPong(event, new WrapperPlayClientPong(event).getId());
        } else if (event.getPacketType() == PacketType.Configuration.Client.PONG) {
            // Same payload as play pong (int id); configuration phase can still carry common pong before PLAY.
            handlePlayOrConfigPong(event, new WrapperConfigClientPong(event).getId());
        }
    }

    /**
     * Vanilla {@code ServerboundPongPacket} is a common type registered in both configuration and play protocol
     * bundles. PE surfaces it as play or config depending on decoder state.
     */
    private static void handlePlayOrConfigPong(PacketReceiveEvent event, int id) {
        GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
        if (player == null) {
            return;
        }
        player.packetStateData.lastTransactionPacketWasValid = false;
        if (id != (short) id) {
            return;
        }
        short shortID = (short) id;
        boolean matched = player.addTransactionResponse(shortID);
        if (shortID <= 0) {
            LegacyLinkGrimTransactionDebug.logPong(event.getUser().getProfile().getName(), shortID, matched);
        }
        if (matched) {
            player.packetStateData.lastTransactionPacketWasValid = true;
            event.setCancelled(!GrimAPI.INSTANCE.getConfigManager().isDisablePongCancelling());
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastServerTransWasValid = false;
            try {
                WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
                short id = confirmation.getActionId();
                // Vanilla always uses an ID starting from 1
                if (id <= 0) {
                    if (player.didWeSendThatTrans.remove(id)) {
                        player.transactionsSent.add(new Pair<>(id, System.nanoTime()));
                        player.lastTransactionSent.getAndIncrement();
                        player.packetStateData.lastServerTransWasValid = true;
                    }
                }
            } catch (Exception e) {
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.PING) {
            WrapperPlayServerPing pong = new WrapperPlayServerPing(event);
            int id = pong.getId();
            //
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;
            player.packetStateData.lastServerTransWasValid = false;
            // Check if in the short range, we only use short range
            if (id == (short) id) {
                // Cast ID twice so we can use the list
                Short shortID = ((short) id);
                if (player.didWeSendThatTrans.remove(shortID)) {
                    player.packetStateData.lastServerTransWasValid = true;
                    player.transactionsSent.add(new Pair<>(shortID, System.nanoTime()));
                    player.lastTransactionSent.getAndIncrement();
                }
            }
        }
    }
}
