package ac.reaper.reaperac.predictionengine;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.reaper.reaperac.utils.data.attribute.ValuedAttribute;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntity;
import ac.reaper.reaperac.utils.enums.FluidTag;
import ac.reaper.reaperac.utils.enums.Pose;
import ac.reaper.reaperac.utils.latency.CompensatedEntities;
import ac.reaper.reaperac.utils.math.GrimMath;
import ac.reaper.reaperac.utils.math.Vector3dm;
import ac.reaper.reaperac.utils.nmsutil.BlockProperties;
import ac.reaper.reaperac.utils.nmsutil.CheckIfChunksLoaded;
import ac.reaper.reaperac.utils.nmsutil.Collisions;
import ac.reaper.reaperac.utils.nmsutil.FluidTypeFlowing;
import ac.reaper.reaperac.utils.nmsutil.GetBoundingBox;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.attributes.EnvironmentAttributes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public final class PlayerBaseTick {

    public static boolean canEnterPose(GrimPlayer player, Pose pose, double x, double y, double z) {
        return Collisions.isEmpty(player, getBoundingBoxForPose(player, pose, x, y, z).expand(-1.0E-7D));
    }

    private static SimpleCollisionBox getBoundingBoxForPose(GrimPlayer player, Pose pose, double x, double y, double z) {
        final float scale = (float) player.compensatedEntities.self.getAttributeValue(Attributes.SCALE);
        final float width = pose.width * scale;
        final float height = pose.height * scale;
        float radius = width / 2.0F;
        return new SimpleCollisionBox(x - radius, y, z - radius, x + radius, y + height, z + radius, false);
    }

    public static void doBaseTick(GrimPlayer player) {
        // Keep track of basetick stuff
        player.baseTickAddition = new Vector3dm();
        player.baseTickWaterPushing = new Vector3dm();

        if (player.isFlying && player.isSneaking && !player.inVehicle()) {
            Vector3dm flyingShift = new Vector3dm(0, player.flySpeed * -3, 0);
            player.baseTickAddVector(flyingShift);
            player.trackBaseTickAddition(flyingShift);
        }

        player.wasEyeInWater = player.fluidInteraction.isEyeInFluid(FluidTag.WATER);
        updateFluidInteraction(player);
        updateSwimming(player);

        // If in lava, fall distance is multiplied by 0.5
        if (player.wasTouchingLava)
            player.fallDistance *= 0.5;

        // You cannot crouch while flying, only shift - could be specific to 1.14?
        // pre-1.13 clients don't have this code
        if (player.wasTouchingWater && player.isSneaking && !player.isFlying && !player.inVehicle()) {
            Vector3dm waterPushVector = new Vector3dm(0, -0.04f, 0);
            player.baseTickAddVector(waterPushVector);
            player.trackBaseTickAddition(waterPushVector);
        }

        player.lastPose = player.pose;

        player.isSlowMovement =
                !player.wasFlying && !player.isSwimming && canEnterPose(player, Pose.CROUCHING, player.lastX, player.lastY, player.lastZ)
                        && (player.wasSneaking || !player.isInBed && !canEnterPose(player, Pose.STANDING, player.lastX, player.lastY, player.lastZ)) ||
                        // If the player is in the swimming pose
                        // Or if the player is not gliding, and the player's pose is fall flying
                        // and the player is not touching water (yes, this also can override the gliding slowness)
                        ((player.pose == Pose.SWIMMING || (!player.isGliding && player.pose == Pose.FALL_FLYING)) && !player.wasTouchingWater);


        if (player.inVehicle()) player.isSlowMovement = false;

        // Players in boats don't care about being in blocks
        if (!player.inVehicle()) {
            moveTowardsClosestSpace(player, player.lastX - (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ + (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX - (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ - (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX + (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ - (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX + (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ + (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
        }

    }

    private static final boolean SERVER_SUPPORT_ENVIRONMENT_ATTRIBUTES = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_11);

    public static void updatePowderSnow(GrimPlayer player) {

        final ValuedAttribute playerSpeed = player.compensatedEntities.self.getAttribute(Attributes.MOVEMENT_SPEED).orElseThrow();

        // Might be null after respawn?
        final Optional<WrapperPlayServerUpdateAttributes.Property> property = playerSpeed.property();
        if (property.isEmpty()) return;

        // The client first desync's this attribute
        property.get().getModifiers().removeIf(modifier -> modifier.getUUID().equals(CompensatedEntities.SNOW_MODIFIER_UUID) || modifier.getName().getKey().equals("powder_snow"));
        playerSpeed.recalculate();

        // And then re-adds it using purely what the server has sent it
        StateType type = BlockProperties.getOnPos(player, player.mainSupportingBlockData, new Vector3d(player.x, player.y, player.z));

        if (!type.isAir()) {
            int i = player.powderSnowFrozenTicks;
            if (i > 0) {
                int ticksToFreeze = 140;
                // Remember, floats are not commutative, we must do it in the client's specific order
                float percentFrozen = (float) Math.min(i, ticksToFreeze) / (float) ticksToFreeze;
                float percentFrozenReducedToSpeed = -0.05F * percentFrozen;

                property.get().getModifiers().add(new WrapperPlayServerUpdateAttributes.PropertyModifier(CompensatedEntities.SNOW_MODIFIER_UUID, percentFrozenReducedToSpeed, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION));
                playerSpeed.recalculate();
            }
        }
    }

    // 1.14
    public static void updatePlayerPose(GrimPlayer player) {
        if (canEnterPose(player, Pose.SWIMMING, player.x, player.y, player.z)) {
            Pose pose;
            if (player.isGliding) {
                pose = Pose.FALL_FLYING;
            } else if (player.isInBed) {
                pose = Pose.SLEEPING;
            } else if (player.isSwimming) {
                pose = Pose.SWIMMING;
            } else if (player.isRiptidePose) {
                pose = Pose.SPIN_ATTACK;
            } else {
                if (player.isSneaking && !player.isFlying) {
                    pose = Pose.CROUCHING;
                } else {
                    pose = Pose.STANDING;
                }
            }

            if (!player.inVehicle() && !canEnterPose(player, pose, player.x, player.y, player.z)) {
                if (canEnterPose(player, Pose.CROUCHING, player.x, player.y, player.z)) {
                    pose = Pose.CROUCHING;
                } else {
                    pose = Pose.SWIMMING;
                }
            }

            player.pose = pose;
            player.boundingBox = getBoundingBoxForPose(player, player.pose, player.x, player.y, player.z);
        }
    }

    private static void updateSwimming(GrimPlayer player) {
        // This doesn't seem like the right place for determining swimming, but it's fine for now
        if (player.isFlying) {
            player.isSwimming = false;
        } else {
            if (player.inVehicle()) {
                player.isSwimming = false;
            } else if (player.isSwimming) {
                player.isSwimming = player.lastSprinting && player.wasTouchingWater;
            } else {
                // Requirement added in 1.17 to fix player glitching between two swimming states
                // while swimming with feet in air and eyes in water
                boolean feetInWater = player.compensatedWorld.getWaterFluidLevelAt(player.lastX, player.lastY, player.lastZ) > 0;
                player.isSwimming = player.lastSprinting && player.wasEyeInWater && player.wasTouchingWater && feetInWater;
            }
        }
    }

    private static void moveTowardsClosestSpace(GrimPlayer player, double xPosition, double zPosition) {
        double movementThreshold = player.getMovementThreshold();
        player.boundingBox = player.boundingBox.expand(movementThreshold, 0, movementThreshold); // 0.03... thanks mojang!
        moveTowardsClosestSpaceModern(player, xPosition, zPosition);
        player.boundingBox = player.boundingBox.expand(-movementThreshold, 0, -movementThreshold);
    }

    // 1.14+
    private static void moveTowardsClosestSpaceModern(GrimPlayer player, double xPosition, double zPosition) {
        int blockX = (int) Math.floor(xPosition);
        int blockZ = (int) Math.floor(zPosition);

        if (!suffocatesAt(player, blockX, blockZ)) {
            return;
        }

        double relativeXMovement = xPosition - blockX;
        double relativeZMovement = zPosition - blockZ;
        BlockFace direction = null;
        double lowestValue = Double.MAX_VALUE;
        for (BlockFace direction2 : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH}) {
            double d6;
            double d7 = direction2 == BlockFace.WEST || direction2 == BlockFace.EAST ? relativeXMovement : relativeZMovement;
            d6 = direction2 == BlockFace.EAST || direction2 == BlockFace.SOUTH ? 1.0 - d7 : d7;
            // d7 and d6 flip the movement direction based on desired movement direction
            boolean doesSuffocate = switch (direction2) {
                case EAST -> suffocatesAt(player, blockX + 1, blockZ);
                case WEST -> suffocatesAt(player, blockX - 1, blockZ);
                case NORTH -> suffocatesAt(player, blockX, blockZ - 1);
                default -> suffocatesAt(player, blockX, blockZ + 1);
            };

            if (d6 >= lowestValue || doesSuffocate) continue;
            lowestValue = d6;
            direction = direction2;
        }
        if (direction != null) {
            if (direction == BlockFace.WEST || direction == BlockFace.EAST) {
                player.uncertaintyHandler.xPositiveUncertainty += 0.15;
                player.uncertaintyHandler.xNegativeUncertainty -= 0.15;
                player.pointThreeEstimator.setPushing(true);
            } else {
                player.uncertaintyHandler.zPositiveUncertainty += 0.15;
                player.uncertaintyHandler.zNegativeUncertainty -= 0.15;
                player.pointThreeEstimator.setPushing(true);
            }
        }
    }

    public static void updateInWaterStateAndDoWaterCurrentPushing(GrimPlayer player) {
        final PacketEntity riding = player.compensatedEntities.self.getRiding();
        player.wasWasTouchingWater = player.wasTouchingWater;
        player.wasTouchingWater = updateFluidHeightAndDoFluidPushing(player, FluidTag.WATER, 0.014) && !(riding != null && riding.isBoat);
        if (player.wasTouchingWater)
            player.fallDistance = 0;
    }

    private static boolean updateFluidHeightAndDoFluidPushing(GrimPlayer player, FluidTag tag, double multiplier) {
        return updateFluidHeightAndDoFluidPushingModern(player, tag, multiplier);

    }

    private static boolean updateFluidHeightAndDoFluidPushingModern(GrimPlayer player, FluidTag tag, double multiplier) {
        SimpleCollisionBox aABB = player.boundingBox.copy().expand(-0.001);

        int floorX = GrimMath.floor(aABB.minX);
        int ceilX = GrimMath.ceil(aABB.maxX);
        int floorY = GrimMath.floor(aABB.minY);
        int ceilY = GrimMath.ceil(aABB.maxY);
        int floorZ = GrimMath.floor(aABB.minZ);
        int ceilZ = GrimMath.ceil(aABB.maxZ);
        if (CheckIfChunksLoaded.areChunksUnloadedAt(player, floorX, floorY, floorZ, ceilX, ceilY, ceilZ)) {
            return false;
        }
        double d2 = 0.0;
        boolean hasTouched = false;
        boolean pushedByFluid = player.isPushedByFluid();
        Vector3dm vec3 = new Vector3dm();
        int n7 = 0;

        for (int x = floorX; x < ceilX; ++x) {
            for (int y = floorY; y < ceilY; ++y) {
                for (int z = floorZ; z < ceilZ; ++z) {
                    double fluidHeightToWorld;

                    double fluidHeight;
                    if (tag == FluidTag.WATER) {
                        fluidHeight = player.compensatedWorld.getWaterFluidLevelAt(x, y, z);
                    } else {
                        fluidHeight = player.compensatedWorld.getLavaFluidLevelAt(x, y, z);
                    }

                    if (fluidHeight == 0 || (fluidHeightToWorld = y + fluidHeight) < aABB.minY)
                        continue;

                    hasTouched = true;
                    d2 = Math.max(fluidHeightToWorld - aABB.minY, d2);

                    if (pushedByFluid) {
                        Vector3dm vec32 = FluidTypeFlowing.getFlow(player, x, y, z);
                        if (d2 < 0.4) {
                            vec32 = vec32.multiply(d2);
                        }
                        vec3 = vec3.add(vec32);
                        ++n7;
                    }
                }
            }
        }

        if (vec3.lengthSquared() > 0.0) {
            if (n7 > 0) {
                vec3 = vec3.multiply(1.0 / n7);
            }

            if (player.inVehicle()) {
                // This is a riding entity, normalize it for some reason.
                vec3 = vec3.normalize();
            }
            vec3 = vec3.multiply(multiplier);
            // Store the vector before handling 0.003, so knockback can use it
            // However, do this after the multiplier, so that we don't have to recompute it
            player.baseTickAddWaterPushing(vec3);
            if (Math.abs(player.clientVelocity.getX()) < 0.003 && Math.abs(player.clientVelocity.getZ()) < 0.003 && vec3.length() < 0.0045000000000000005D) {
                vec3 = vec3.normalize().multiply(0.0045000000000000005);
            }

            player.baseTickAddVector(vec3);
        }

        player.fluidHeight.put(tag, d2);
        return hasTouched;
    }

    public static boolean updateFluidInteraction(GrimPlayer player) {
        player.fluidInteraction.update(player, !player.isPushedByFluid());
        boolean inWater = player.fluidInteraction.isInFluid(FluidTag.WATER);
        boolean inLava = player.fluidInteraction.isInFluid(FluidTag.LAVA);
        if (inWater) {
            player.fallDistance = 0;
        }

        player.wasWasTouchingWater = player.wasTouchingWater;
        player.wasTouchingWater = inWater;
        player.wasTouchingLava = inLava;
        if (player.isPushedByFluid()) {
            if (inWater) {
                player.fluidInteraction.applyCurrentTo(FluidTag.WATER, player, 0.014);
            }

            if (inLava) {
                final boolean fastLava = SERVER_SUPPORT_ENVIRONMENT_ATTRIBUTES
                        ? player.dimensionType.getAttributes().getOrDefault(EnvironmentAttributes.GAMEPLAY_FAST_LAVA)
                        : player.dimensionType.isUltraWarm();

                final double scale = fastLava ? 0.007 : 0.0023333333333333335;
                player.fluidInteraction.applyCurrentTo(FluidTag.LAVA, player, scale);
            }
        }

        return inWater || inLava;
    }

    private static boolean suffocatesAt(GrimPlayer player, int x, int z) {
        SimpleCollisionBox axisAlignedBB = new SimpleCollisionBox(x, player.boundingBox.minY, z, x + 1.0, player.boundingBox.maxY, z + 1.0, false).expand(-1.0E-7);
        return Collisions.suffocatesAt(player, axisAlignedBB);
    }

}
