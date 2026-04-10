package ac.reaper.reaperac.utils.data.packetentity;

import ac.reaper.reaperac.checks.impl.sprint.SprintD;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.reaper.reaperac.utils.data.attribute.ValuedAttribute;
import ac.reaper.reaperac.utils.inventory.EnchantmentHelper;
import ac.reaper.reaperac.utils.math.GrimMath;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionType;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.ArrayList;

public class PacketEntitySelf extends PacketEntity {

    private final GrimPlayer player;
    public int opLevel;

    public PacketEntitySelf(GrimPlayer player) {
        super(player, EntityTypes.PLAYER);
        this.player = player;
    }

    public PacketEntitySelf(GrimPlayer player, PacketEntitySelf old) {
        super(player, EntityTypes.PLAYER);
        this.player = player;
        this.opLevel = old.opLevel;
        this.attributeMap.putAll(old.attributeMap);
    }

    @Override
    protected void initAttributes(GrimPlayer player) {
        super.initAttributes(player);

        getAttribute(Attributes.SCALE).orElseThrow().withSetRewriter((oldValue, newValue) -> {
            if (newValue.equals(oldValue)) {
                return oldValue;
            } else {
                // Elytra, standing, sneaking (1.14)
                player.possibleEyeHeights[2][0] = 0.4 * newValue;
                player.possibleEyeHeights[2][1] = 1.62 * newValue;
                player.possibleEyeHeights[2][2] = 1.27 * newValue;

                // sneaking (1.14), standing, Elytra
                player.possibleEyeHeights[1][0] = 1.27 * newValue;
                player.possibleEyeHeights[1][1] = 1.62 * newValue;
                player.possibleEyeHeights[1][2] = 0.4 * newValue;

                // standing, sneaking (1.14), Elytra
                player.possibleEyeHeights[0][0] = 1.62 * newValue;
                player.possibleEyeHeights[0][1] = 1.27 * newValue;
                player.possibleEyeHeights[0][2] = 0.4 * newValue;
                return newValue;
            }
        });

        final ValuedAttribute movementSpeed = ValuedAttribute.ranged(Attributes.MOVEMENT_SPEED, 0.1f, 0, 1024);
        movementSpeed.with(new WrapperPlayServerUpdateAttributes.Property(Attributes.MOVEMENT_SPEED, 0.1f, new ArrayList<>()));
        trackAttribute(movementSpeed);
        trackAttribute(ValuedAttribute.ranged(Attributes.ATTACK_DAMAGE, 2, 0, 2048)); // NOTE: Not synced to client currently.
        trackAttribute(ValuedAttribute.ranged(Attributes.ATTACK_SPEED, 4, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_9));
        trackAttribute(ValuedAttribute.ranged(Attributes.JUMP_STRENGTH, 0.42f, 0, 32)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.BLOCK_BREAK_SPEED, 1.0, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.MINING_EFFICIENCY, 0, 0, 1024)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.SUBMERGED_MINING_SPEED, 0.2, 0, 20)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.ENTITY_INTERACTION_RANGE, 3, 0, 64)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.BLOCK_INTERACTION_RANGE, 4.5, 0, 64)
                .requiredVersion(player, ClientVersion.V_1_20_5));
        trackAttribute(ValuedAttribute.ranged(Attributes.WATER_MOVEMENT_EFFICIENCY, 0, 0, 1)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.MOVEMENT_EFFICIENCY, 0, 0, 1)
                .requiredVersion(player, ClientVersion.V_1_21));
        trackAttribute(ValuedAttribute.ranged(Attributes.AIR_DRAG_MODIFIER, 1.0, 0, 2048)
                .requiredVersion(player, ClientVersion.V_26_2));
        trackAttribute(ValuedAttribute.ranged(Attributes.BOUNCINESS, 0.0, 0, 1)
                .requiredVersion(player, ClientVersion.V_26_2));
        trackAttribute(ValuedAttribute.ranged(Attributes.FRICTION_MODIFIER, 1.0, 0, 2048)
                .requiredVersion(player, ClientVersion.V_26_2));
        trackAttribute(ValuedAttribute.ranged(Attributes.SNEAKING_SPEED, 0.3, 0, 1)
                .requiredVersion(player, ClientVersion.V_1_21));
    }

    public boolean inVehicle() {
        return getRiding() != null;
    }

    @Override
    public void addPotionEffect(PotionType effect, int amplifier) {
        if (effect == PotionTypes.BLINDNESS && !hasPotionEffect(PotionTypes.BLINDNESS)) {
            player.checkManager.getPostPredictionCheck(SprintD.class).startedSprintingBeforeBlind = player.isSprinting;
        }

        player.pointThreeEstimator.updatePlayerPotions(effect, amplifier);
        super.addPotionEffect(effect, amplifier);
    }

    @Override
    public void removePotionEffect(PotionType effect) {
        player.pointThreeEstimator.updatePlayerPotions(effect, null);
        super.removePotionEffect(effect);
    }

    @Override
    public void onFirstTransaction(boolean relative, boolean hasPos, double relX, double relY, double relZ, GrimPlayer player) {
        // Player ignores this
    }

    @Override
    public void onSecondTransaction() {
        // Player ignores this
    }

    @Override
    public SimpleCollisionBox getPossibleCollisionBoxes() {
        return player.boundingBox.copy(); // Copy to retain behavior of PacketEntity
    }
}
