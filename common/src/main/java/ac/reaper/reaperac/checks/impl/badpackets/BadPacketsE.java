package ac.reaper.reaperac.checks.impl.badpackets;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "BadPacketsE")
public class BadPacketsE extends Check implements PacketCheck {
    private int noReminderTicks;
    private final int maxNoReminderTicks;

    {
        player.getClientVersion();
        maxNoReminderTicks = 19;
    }

    public BadPacketsE(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION ||
                event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            noReminderTicks = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !player.packetStateData.lastPacketWasTeleport) {
            if (++noReminderTicks > maxNoReminderTicks) {
                flagAndAlert("ticks=" + noReminderTicks);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE
                || player.inVehicle()) {
            noReminderTicks = 0; // Exempt vehicles
        }
    }

    public void handleRespawn() {
        noReminderTicks = 0;
    }
}
