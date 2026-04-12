package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.events.packets.*;
import ac.reaper.reaperac.events.packets.worldreader.PacketWorldReaderEighteen;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.PacketEvents;

public class PacketManager implements StartableInitable {
    @Override
    public void start() {
        LogUtil.info("Registering packets...");

        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerJoinQuit());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPingListener());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerDigging());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerAttack());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEntityAction());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketBlockAction());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketSelfMetadataListener());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketServerTeleport());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerCooldown());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerRespawn());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerTick());
        PacketEvents.getAPI().getEventManager().registerListener(new CheckManagerListener());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketPlayerSteer());
        // Packet payload capture/audit listeners are intentionally disabled in production.

        PacketEvents.getAPI().getEventManager().registerListener(new PacketServerTags());

        PacketEvents.getAPI().getEventManager().registerListener(new PacketWorldReaderEighteen());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketSpawnerSanitizer());

        PacketEvents.getAPI().getEventManager().registerListener(new ProxyAlertMessenger());
        PacketEvents.getAPI().getEventManager().registerListener(new PacketHidePlayerInfo());

        PacketEvents.getAPI().init();
    }
}
