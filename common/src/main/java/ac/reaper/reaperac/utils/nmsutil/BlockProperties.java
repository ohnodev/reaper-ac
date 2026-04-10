package ac.reaper.reaperac.utils.nmsutil;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.MainSupportingBlockData;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntity;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityHorse;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityNautilus;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityStrider;
import ac.reaper.reaperac.utils.math.GrimMath;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BlockProperties {
    public static float getFrictionInfluencedSpeed(float f, GrimPlayer player) {
        if (player.lastOnGround) {
            return (float) (player.speed * (0.21600002f / (f * f * f)));
        }

        // The game uses values known as flyingSpeed for some vehicles in the air
        if (player.inVehicle()) {
            PacketEntity riding = player.compensatedEntities.self.getRiding();
            if (riding.type == EntityTypes.PIG || riding instanceof PacketEntityNautilus || riding instanceof PacketEntityHorse) {
                return (float) (player.speed * 0.1f);
            }

            if (riding instanceof PacketEntityStrider) {
                return (float) player.speed * 0.1f; // Vanilla multiplies by 0.1 to calculate speed
            }
        }

        if (player.isFlying) {
            return player.flySpeed * 20 * (player.isSprinting ? 0.1f : 0.05f);
        }

        // In 1.19.4, air sprinting is based on current sprinting, not last sprinting

        return player.isSprinting ? 0.025999999F : 0.02f;

    }

    /**
     * This is used for falling onto a block (We care if there is a bouncy block)
     * This is also used for striders checking if they are on lava
     * <p>
     * For soul speed (server-sided only)
     * (we don't account for this and instead remove this debuff) And powder snow block attribute
     */
    public static StateType getOnPos(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {


        Vector3i pos = getOnPos(player, playerPos, mainSupportingBlockData, 0.2F);
        return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
    }

    public static float getFriction(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
        return getMaterialFriction(player, underPlayer);
    }

    public static float getBlockSpeedFactor(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        // This system was introduces in 1.15 players to add support for honey blocks slowing players down
        if (player.isGliding || player.isFlying) return 1.0f;

        WrappedBlockState inBlock = player.compensatedWorld.getBlock(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        float inBlockSpeedFactor = getBlockSpeedFactor(player, inBlock.getType());
        if (inBlockSpeedFactor != 1.0f || inBlock.getType() == StateTypes.WATER || inBlock.getType() == StateTypes.BUBBLE_COLUMN) {
            return getModernVelocityMultiplier(player, inBlockSpeedFactor);
        }

        StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
        return getModernVelocityMultiplier(player, getBlockSpeedFactor(player, underPlayer));
    }

    public static boolean onHoneyBlock(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
        StateType inBlock = player.compensatedWorld.getBlockType(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        return inBlock == StateTypes.HONEY_BLOCK || getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos) == StateTypes.HONEY_BLOCK;
    }

    /**
     * Friction
     * Block jump factor
     * Block speed factor
     * <p>
     * On soul speed block (server-sided only)
     */
    private static StateType getBlockPosBelowThatAffectsMyMovement(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {

        Vector3i pos = getOnPos(player, playerPos, mainSupportingBlockData, 0.500001F);
        return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
    }

    private static Vector3i getOnPos(GrimPlayer player, Vector3d playerPos, MainSupportingBlockData mainSupportingBlockData, float searchBelowPlayer) {
        Vector3i mainBlockPos = mainSupportingBlockData.blockPos();
        if (mainBlockPos != null) {
            StateType blockstate = player.compensatedWorld.getBlockType(mainBlockPos.x, mainBlockPos.y, mainBlockPos.z);

            // I genuinely don't understand this code, or why fences are special
            boolean shouldReturn = (!((double) searchBelowPlayer <= 0.5D) || !BlockTags.FENCES.contains(blockstate)) &&
                    !BlockTags.WALLS.contains(blockstate) &&
                    !BlockTags.FENCE_GATES.contains(blockstate);

            return shouldReturn ? mainBlockPos.withY(GrimMath.floor(playerPos.getY() - (double) searchBelowPlayer)) : mainBlockPos;
        } else {
            return new Vector3i(GrimMath.floor(playerPos.getX()), GrimMath.floor(playerPos.getY() - searchBelowPlayer), GrimMath.floor(playerPos.getZ()));
        }
    }

    public static float getMaterialFriction(GrimPlayer player, StateType material) {
        float friction = 0.6f;

        if (material == StateTypes.ICE) friction = 0.98f;
        if (material == StateTypes.SLIME_BLOCK) friction = 0.8f;
        if (material == StateTypes.PACKED_ICE) friction = 0.98f;
        if (material == StateTypes.FROSTED_ICE) friction = 0.98f;
        if (material == StateTypes.BLUE_ICE) friction = 0.989f;

        return friction;
    }

    private static StateType getOnBlock(GrimPlayer player, double x, double y, double z) {
        StateType block1 = player.compensatedWorld.getBlockType(GrimMath.floor(x), GrimMath.floor(y - 0.2F), GrimMath.floor(z));

        if (block1.isAir()) {
            StateType block2 = player.compensatedWorld.getBlockType(GrimMath.floor(x), GrimMath.floor(y - 1.2F), GrimMath.floor(z));

            if (Materials.isFence(block2) || Materials.isWall(block2) || Materials.isGate(block2)) {
                return block2;
            }
        }

        return block1;
    }

    private static float getBlockSpeedFactor(GrimPlayer player, StateType type) {
        if (type == StateTypes.HONEY_BLOCK) return 0.4f;
        if (type == StateTypes.SOUL_SAND) {
            // Soul speed is a 1.16+ enchantment
            // This new method for detecting soul speed was added in 1.16.2
            // On 1.21, let attributes handle this

            return 0.4f;
        }
        return 1.0f;
    }

    private static float getModernVelocityMultiplier(GrimPlayer player, float blockSpeedFactor) {
        return (float) GrimMath.lerp((float) player.compensatedEntities.self.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), blockSpeedFactor, 1.0F);
    }
}
