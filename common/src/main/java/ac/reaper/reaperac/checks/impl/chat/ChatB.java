package ac.reaper.reaperac.checks.impl.chat;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommandUnsigned;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;

// this can false from click events, but I doubt this would actually
// happen unless they're trying to flag, or if the server is set up badly
@CheckData(name = "ChatB", description = "Invalid chat message")
public class ChatB extends Check implements PacketCheck {
    public ChatB(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            String message = new WrapperPlayClientChatMessage(event).getMessage();
            if (checkChatMessage(message)) {
                event.setCancelled(true);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND_UNSIGNED) {
            String command = "/" + new WrapperPlayClientChatCommandUnsigned(event).getCommand();
            if (!command.stripTrailing().equals(command)) {
                if (flagAndAlert("command=" + command)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CHAT_COMMAND) {
            String command;
            try {
                command = "/" + new WrapperPlayClientChatCommand(event).getCommand();
            } catch (Exception e) {
                return;
            }
            if (!command.trim().equals(command)) {
                if (flagAndAlert("command=" + command)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }

    // returns whether the packet should be cancelled
    public boolean checkChatMessage(String message) {
        if (message.isEmpty() || !message.trim().equals(message) || message.startsWith("/")) {
            if (flagAndAlert("message=" + message) && shouldModifyPackets()) {
                player.onPacketCancel();
                return true;
            }
        }
        return false;
    }
}
