package ac.reaper.reaperac.events.packets;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.checks.impl.badpackets.BadPacketsW;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntity;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityHorse;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectateEntity;

public class PacketPlayerAttack extends PacketListenerAbstract {

    public PacketPlayerAttack() {
        super(PacketListenerPriority.LOW);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());

            if (player == null) return;

            int entityId = interact.getEntityId();

            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                onAttack(event, player, entityId);
            } else if (isInvalidEntity(event, player, entityId)) return;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if (packet.getAction() == DiggingAction.STAB) {
                GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
                if (player == null) return;

                if (player.isResetItemUsageOnAttack()) {
                    GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(player.platformPlayer);
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
            onAttack(event, player, packet.getEntityId());
        }

        if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
            GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (player == null) return;

            WrapperPlayClientSpectateEntity packet = new WrapperPlayClientSpectateEntity(event);
            onAttack(event, player, packet.getEntityId());
        }
    }

    private void onAttack(PacketReceiveEvent event, GrimPlayer player, int entityId) {
        if (isInvalidEntity(event, player, entityId)) return;

        if (player.isResetItemUsageOnAttack()) {
            GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(player.platformPlayer);
        }

        // This is not vanilla behaviour as the attack damage attribute is marked as not synced to the client
        // However, plugins can still set this by sending an attributes packet
        if (player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 0) return;

        ItemStack heldItem = player.inventory.getHeldItem();
        PacketEntity entity = player.compensatedEntities.getEntity(entityId);

        if (entity != null && (!entity.isLivingEntity || entity.type == EntityTypes.PLAYER || entity.type == EntityTypes.PAINTING)) {
            final boolean hasNegativeKB = false;
            final boolean sufficientCooldownProgress = player.attackCooldown.getMinimumProgress() > 0.9F;

            // 1.8 players who are packet sprinting WILL get slowed
            // 1.9+ players who are packet sprinting might not, based on attack cooldown
            // Players with knockback enchantments always get slowed

            if (player.lastSprinting && !hasNegativeKB && sufficientCooldownProgress) {
                player.minAttackSlow++;
                player.maxAttackSlow++;

                // Players cannot slow themselves twice in one tick without a knockback sword
                player.maxAttackSlow = player.minAttackSlow = 1;
            } else if (player.lastSprinting) {
                // 1.9+ players who have attack speed cannot slow themselves twice in one tick because their attack cooldown gets reset on swing.
                if (player.maxAttackSlow > 0 && player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_SPEED) < 16) return; // 16 is a reasonable limit

                // 1.9+ player who might have been slowed, but we can't be sure
                player.maxAttackSlow++;
            }
        }

        if (player.gamemode != GameMode.SPECTATOR) {
            player.attackCooldown.reset();
        }
    }

    private boolean isInvalidEntity(PacketReceiveEvent event, GrimPlayer player, int entityId) {
        // The entity does not exist
        if (!player.compensatedEntities.entityMap.containsKey(entityId) && !player.compensatedEntities.serverPositionsMap.containsKey(entityId)) {
            final BadPacketsW badPacketsW = player.checkManager.getCheck(BadPacketsW.class);
            if (badPacketsW.flagAndAlert("entityId=" + entityId) && badPacketsW.shouldModifyPackets()) {
                event.setCancelled(true);
                player.onPacketCancel();
            }
            return true;
        }
        return false;
    }
}
