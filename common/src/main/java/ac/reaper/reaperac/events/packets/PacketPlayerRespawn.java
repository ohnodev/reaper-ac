package ac.reaper.reaperac.events.packets;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.checks.impl.badpackets.BadPacketsE;
import ac.reaper.reaperac.checks.impl.badpackets.BadPacketsF;
import ac.reaper.reaperac.checks.impl.badpackets.BadPacketsG;
import ac.reaper.reaperac.checks.impl.badpackets.BadPacketsH;
import ac.reaper.reaperac.checks.impl.elytra.ElytraC;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.KnownInput;
import ac.reaper.reaperac.utils.data.TrackerData;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntitySelf;
import ac.reaper.reaperac.utils.enums.Pose;
import ac.reaper.reaperac.utils.math.Vector3dm;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

import java.util.List;
import java.util.Objects;

/**
 * PlayerRespawnS2CPacket info (1.20.2+):
 * If the world is different (check via registry key), world is recreated (all entities etc destroyed).
 * <p>
 * Client player is ALWAYS recreated
 * <p>
 * If the packet has the `KEEP_TRACKED_DATA` flag:
 * Sneaking and Sprinting fields are kept on the new client player.
 * <p>
 * If the packet has the `KEEP_ATTRIBUTES` flag:
 * Attributes are kept.
 * <p>
 * New client player is initialised:
 * Pose is set to standing.
 * Velocity is set to zero.
 * Pitch is set to 0.
 * Yaw is set to -180.
 */
// TODO update for 1.20.2-
public class PacketPlayerRespawn extends PacketListenerAbstract {

    private static final byte KEEP_ATTRIBUTES = 1;
    private static final byte KEEP_TRACKED_DATA = 2;
    private static final byte KEEP_ALL = 3;

    public PacketPlayerRespawn() {
        super(PacketListenerPriority.HIGH);
    }

    private boolean hasFlag(WrapperPlayServerRespawn respawn, byte flag) {
        return (respawn.getKeptData() & flag) != 0;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth health = new WrapperPlayServerUpdateHealth(event);

            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            player.packetStateData.lastFood = health.getFood();
            player.packetStateData.lastHealth = health.getHealth();
            player.packetStateData.lastSaturation = health.getFoodSaturation();

            player.sendTransaction();

            if (health.getFood() == 20) { // Split so transaction before packet
                player.latencyUtils.addRealTimeTask(player.lastTransactionReceived.get(), () -> player.food = 20);
            } else { // Split so transaction after packet
                player.latencyUtils.addRealTimeTask(player.lastTransactionReceived.get() + 1, () -> player.food = health.getFood());
            }

            if (health.getHealth() <= 0) {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> player.compensatedEntities.self.isDead = true);
            } else {
                player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> player.compensatedEntities.self.isDead = false);
            }

            event.getTasksAfterSend().add(player::sendTransaction);
        }

        if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayServerJoinGame joinGame = new WrapperPlayServerJoinGame(event);
            player.gamemode = joinGame.getGameMode();
            player.entityID = joinGame.getEntityId();
            player.dimensionType = joinGame.getDimensionType();
            player.worldName = joinGame.getWorldName();

            player.compensatedWorld.setDimension(joinGame.getDimensionType(), event.getUser());
        }

        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn(event);

            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            List<Runnable> tasks = event.getTasksAfterSend();
            tasks.add(player::sendTransaction);

            // Force the player to accept a teleport before respawning
            // (We won't process movements until they accept a teleport, we won't let movements though either)
            // Also invalidate previous positions
            player.getSetbackTeleportUtil().hasAcceptedSpawnTeleport = false;
            player.getSetbackTeleportUtil().lastKnownGoodPosition = null;

            // clear server entity positions when the world changes
            if (isWorldChange(player, respawn)) {
                player.compensatedEntities.serverPositionsMap.clear();
            }

            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> {
                // From 1.16 to 1.19, this doesn't get set to false for whatever reason
                player.getClientVersion();
                player.getClientVersion();
                player.isSneaking = false;

                player.lastOnGround = false;
                player.clientClaimsLastOnGround = false;
                player.onGround = false;
                player.isInBed = false;
                player.packetStateData.setSlowedByUsingItem(false);
                player.packetStateData.packetPlayerOnGround = false; // If somewhere else pulls last ground to fix other issues
                player.packetStateData.lastClaimedPosition = new Vector3d();
                player.filterMojangStupidityOnMojangStupidity = new Vector3d();

                final boolean keepTrackedData = this.hasFlag(respawn, KEEP_TRACKED_DATA);

                if (!keepTrackedData) {
                    player.food = 20;
                    player.powderSnowFrozenTicks = 0;
                    player.compensatedEntities.self.hasGravity = true;
                    player.playerEntityHasGravity = true;
                    player.packetStateData.knownInput = KnownInput.DEFAULT;
                    player.checkManager.getPostPredictionCheck(ElytraC.class).exempt = true;

                    // 1.19.4 uses current sprinting, older versions use last sprinting
                    player.getClientVersion();
                    player.isSprinting = false;
                }

                player.checkManager.getPacketCheck(BadPacketsE.class).handleRespawn(); // Reminder ticks reset
                player.checkManager.getPacketCheck(BadPacketsG.class).handleRespawn();

                // compensate for immediate respawn gamerule
                player.getClientVersion();
                player.checkManager.getPacketCheck(BadPacketsF.class).exemptNext = true;

                // EVERYTHING gets reset on a cross dimensional teleport, clear chunks and entities!
                if (isWorldChange(player, respawn)) {
                    player.compensatedEntities.entityMap.clear();
                    player.compensatedWorld.activePistons.clear();
                    player.compensatedWorld.openShulkerBoxes.clear();
                    player.compensatedWorld.chunks.clear();
                    player.compensatedWorld.isRaining = false;
                    player.checkManager.getBlockPlaceCheck(BadPacketsH.class).onWorldChange();
                }
                player.dimensionType = respawn.getDimensionType();
                player.worldName = respawn.getWorldName().orElse(null);

                player.compensatedEntities.serverPlayerVehicle = null; // All entities get removed on respawn
                player.compensatedEntities.self = new PacketEntitySelf(player, player.compensatedEntities.self);
                player.compensatedEntities.selfTrackedEntity = new TrackerData(0, 0, 0, 0, 0, EntityTypes.PLAYER, player.lastTransactionSent.get());

                player.getClientVersion();
                player.pose = Pose.STANDING;
                player.clientVelocity = new Vector3dm();
                if (!GrimAPI.INSTANCE.getSpectateManager().isSpectating(player.uuid)) {
                    player.gamemode = respawn.getGameMode();
                }

                player.compensatedWorld.setDimension(respawn.getDimensionType(), event.getUser());

                player.getClientVersion();
                if (!this.hasFlag(respawn, KEEP_ATTRIBUTES)) {
                    // Reset attributes if not kept
                    player.compensatedEntities.self.resetAttributes();
                    player.compensatedEntities.hasSprintingAttributeEnabled = false;
                }
            });
        }
    }

    private boolean isWorldChange(GrimPlayer player, WrapperPlayServerRespawn respawn) {
        player.getClientVersion();
        return !Objects.equals(respawn.getWorldName().orElse(null), player.worldName);

    }
}
