package ac.reaper.reaperac.predictionengine.movementtick;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.predictionengine.PlayerBaseTick;
import ac.reaper.reaperac.predictionengine.predictions.PredictionEngine;
import ac.reaper.reaperac.predictionengine.predictions.PredictionEngineElytra;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.reaper.reaperac.utils.data.VectorData;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntity;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityStrider;
import ac.reaper.reaperac.utils.enums.FluidTag;
import ac.reaper.reaperac.utils.math.GrimMath;
import ac.reaper.reaperac.utils.math.Vector3dm;
import ac.reaper.reaperac.utils.nmsutil.BlockProperties;
import ac.reaper.reaperac.utils.nmsutil.Collisions;
import ac.reaper.reaperac.utils.nmsutil.EntityTypeTags;
import ac.reaper.reaperac.utils.nmsutil.FluidFallingAdjustedMovement;
import ac.reaper.reaperac.utils.nmsutil.GetBoundingBox;
import ac.reaper.reaperac.utils.nmsutil.MainSupportingBlockPosFinder;
import ac.reaper.reaperac.utils.team.EntityPredicates;
import ac.reaper.reaperac.utils.team.EntityTeam;
import ac.reaper.reaperac.utils.team.TeamHandler;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MovementTicker {
    public final GrimPlayer player;

    /**
     * Vanilla 26.2+: clamp(1.0 - (1.0 - friction) * modifier, 0.0, 1.0)
     * For pre-26.2 clients the modifier is always 1.0 so this is identity.
     */
    public static float computeModifiedFriction(float friction, double modifier) {
        return (float) GrimMath.clamp(1.0 - (1.0 - friction) * modifier, 0.0, 1.0);
    }

    public static float getAirDrag(GrimPlayer player) {
        double airDragMod = player.compensatedEntities.self.getAttributeValue(Attributes.AIR_DRAG_MODIFIER);
        return computeModifiedFriction(0.91f, airDragMod);
    }

    public static float getBlockFrictionModified(GrimPlayer player, float blockFriction) {
        double frictionMod = player.compensatedEntities.self.getAttributeValue(Attributes.FRICTION_MODIFIER);
        return computeModifiedFriction(blockFriction, frictionMod);
    }

    public static void handleEntityCollisions(GrimPlayer player) {
        // Check that ViaVersion disables all collisions on a 1.8 server for 1.9+ clients
        boolean hasEntityPushing = true;
        if (!hasEntityPushing) return;

        int possibleCollidingEntities = 0;
        int possibleRiptideEntities = 0;

        // Players in vehicles do not have collisions
        if (!player.inVehicle() && player.gamemode != GameMode.SPECTATOR) {
            // Calculate the offset of the player to colliding other stuff
            SimpleCollisionBox playerBox = GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.lastX, player.lastY, player.lastZ, 0.6f, 1.8f);
            playerBox.encompass(GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.x, player.y, player.z, 0.6f, 1.8f).expand(player.getMovementThreshold()));
            playerBox.expand(0.2);

            final TeamHandler teamHandler = player.checkManager.getPacketCheck(TeamHandler.class);
            final EntityTeam playerTeam = teamHandler != null ? teamHandler.getPlayerTeam() : null;
            for (PacketEntity entity : player.compensatedEntities.entityMap.values()) {
                // TODO actually handle entity collisions instead of this awfulness
                SimpleCollisionBox entityBox = entity.getPossibleCollisionBoxes();
                if (!playerBox.isCollided(entityBox)) continue;

                possibleRiptideEntities++;

                if (!entity.isPushable()) continue;

                // Filters out entities that can't be pushed/collided because of team collision rules
                // Also handles 1.9+ player on 1.8- server with ViaVersion prevent-collision disabled.
                final EntityTeam entityTeam = teamHandler != null ? teamHandler.getEntityTeam(entity) : null;
                if (!EntityPredicates.canBePushedBy(entityTeam, playerTeam)) continue;

                possibleCollidingEntities++;
            }
        }

        if (player.isGliding && possibleCollidingEntities > 0) {
            // Horizontal starting movement affects vertical movement with elytra, hack around this.
            // This can likely be reduced but whatever, I don't see this as too much of a problem
            player.uncertaintyHandler.yNegativeUncertainty -= 0.05;
            player.uncertaintyHandler.yPositiveUncertainty += 0.05;
        }

        player.uncertaintyHandler.riptideEntities.add(possibleRiptideEntities);
        player.uncertaintyHandler.collidingEntities.add(possibleCollidingEntities);
    }

    private boolean isHorizontalCollisionSoft(Vector3dm collide) {
        double horizontalLengthSquared = collide.getX() * collide.getX() + collide.getZ() * collide.getZ();
        if (horizontalLengthSquared < 1E-5F) return false;

        float xxa = (float) player.predictedVelocity.input.getX();
        float zza = (float) player.predictedVelocity.input.getZ();

        float yawInRadians = player.yaw * (float) (Math.PI / 180.0);
        double sin = player.trigHandler.sin(yawInRadians);
        double cos = player.trigHandler.cos(yawInRadians);
        double g = xxa * cos - zza * sin;
        double h = zza * cos + xxa * sin;
        double i = g * g + h * h;
        return i >= 1E-5F && Math.acos((g * collide.getX() + h * collide.getZ()) / Math.sqrt(i * horizontalLengthSquared)) < 0.13962634F;
    }

    public void move(Vector3dm inputVel, Vector3dm collide) {
        if (player.stuckSpeedMultiplier.getX() < 0.99) {
            player.clientVelocity = new Vector3dm();
        }

        boolean xAxis = !GrimMath.equal(inputVel.getX(), collide.getX());
        boolean zAxis = !GrimMath.equal(inputVel.getZ(), collide.getZ());

        if (xAxis) {
            player.clientVelocity.setX(0);
        }

        if (zAxis) {
            player.clientVelocity.setZ(0);
        }

        player.horizontalCollision = xAxis || zAxis;
        player.softHorizontalCollision = player.horizontalCollision && isHorizontalCollisionSoft(collide);

        player.verticalCollision = inputVel.getY() != collide.getY();

        // Avoid order of collisions being wrong because 0.03 movements
        // Stepping movement USUALLY means the vehicle in on the ground as vehicles can't jump
        // Can be wrong with swim hopping into step, but this is rare and difficult to pull off
        // and would require a huge rewrite to support this rare edge case
        boolean calculatedOnGround = (player.verticalCollision && inputVel.getY() < 0.0D);

        // If the player is on the ground with a y velocity of 0, let the player decide (too close to call)
        if (inputVel.getY() == -SimpleCollisionBox.COLLISION_EPSILON && collide.getY() > -SimpleCollisionBox.COLLISION_EPSILON && collide.getY() <= 0 && !player.inVehicle())
            calculatedOnGround = player.onGround;
        player.clientClaimsLastOnGround = player.onGround;

        // Fix step movement inside of water
        // Swim hop into step is very unlikely, as step requires y < 0, while swim hop forces y = 0.3
        if (player.inVehicle() && player.clientControlledVerticalCollision && player.uncertaintyHandler.isStepMovement &&
                (inputVel.getY() <= 0 || player.predictedVelocity.isSwimHop())) {
            calculatedOnGround = true;
        }

        // We can't tell the difference between stepping and swim hopping, so just let the player's onGround status be the truth
        // Pistons/shulkers are a bit glitchy so just trust the client when they are affected by them
        // The player's onGround status isn't given when riding a vehicle, so we don't have a choice in whether we calculate or not
        //
        // Trust the onGround status if the player is near the ground and they sent a ground packet
        if (player.inVehicle() || !player.exemptOnGround()) {
            player.onGround = calculatedOnGround;
        }

        // This is around the place where the new bounding box gets set
        player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.x, player.y, player.z);
        // This is how the player checks for fall damage
        // By running fluid pushing for the player
        final PacketEntity riding = player.compensatedEntities.self.getRiding();
        // Re-run fluid interaction when needed for fall-distance and movement consistency.
        if (!player.wasTouchingWater && (riding == null || (!riding.isBoat && !riding.isHappyGhast))) {
            PlayerBaseTick.updateInWaterStateAndDoWaterCurrentPushing(player);
        }

        if (player.onGround) {
            player.fallDistance = 0;
        } else if (collide.getY() < 0) {
            player.fallDistance = (player.fallDistance) - collide.getY();
            player.vehicleData.lastYd = collide.getY();
        }

        // Striders call the method for inside blocks AGAIN!
        if (riding instanceof PacketEntityStrider) {
            Collisions.handleInsideBlocks(player);
        }

        player.mainSupportingBlockData = MainSupportingBlockPosFinder.findMainSupportingBlockPos(player, player.mainSupportingBlockData, new Vector3d(collide.getX(), collide.getY(), collide.getZ()), player.boundingBox, player.onGround);
        StateType onBlock = BlockProperties.getOnPos(player, player.mainSupportingBlockData, new Vector3d(player.x, player.y, player.z));

        // Hack with 1.14+ poses issue
        if (inputVel.getY() != collide.getY()) {
            // If the client supports slime blocks
            // And the block is a slime block
            // Or the block is honey and was replaced by viaversion
            if (onBlock == StateTypes.SLIME_BLOCK) {
                if (player.isSneaking) { // Slime blocks use shifting instead of sneaking
                    player.clientVelocity.setY(0);
                } else {
                    if (player.clientVelocity.getY() < 0.0) {
                        player.clientVelocity.setY(-player.clientVelocity.getY() *
                                (riding != null && !riding.isLivingEntity ? 0.8 : 1.0));
                    }
                }
            } else {
                if (BlockTags.BEDS.contains(onBlock)) {
                    if (player.clientVelocity.getY() < 0.0) {
                        player.clientVelocity.setY(-player.clientVelocity.getY() * 0.6600000262260437 *
                                (riding != null && !riding.isLivingEntity ? 0.8 : 1.0));
                    }
                } else {
                    player.clientVelocity.setY(0);
                }
            }
        }

        collide = PredictionEngine.clampMovementToHardBorder(player, collide);

        // The game disregards movements smaller than 1e-7 (such as in boats)
        // New condition added in 1.21.2
        if (collide.lengthSquared() <= 1e-7 && inputVel.lengthSquared() - collide.lengthSquared() >= 1e-7) {
            collide = new Vector3dm();
        } else {
            Vector3d from = new Vector3d(player.lastX, player.lastY, player.lastZ);
            Vector3d to = new Vector3d(player.x, player.y, player.z);

            player.addMovementThisTick(new GrimPlayer.Movement(from, to, new Vector3d(inputVel.getX(), inputVel.getY(), inputVel.getZ())));
        }

        // This is where vanilla moves the bounding box and sets it
        player.predictedVelocity = new VectorData(collide.clone(), player.predictedVelocity.lastVector, player.predictedVelocity.vectorType);

        float f = BlockProperties.getBlockSpeedFactor(player, player.mainSupportingBlockData, new Vector3d(player.x, player.y, player.z));
        player.clientVelocity.multiply(f, 1, f);
    }

    public void livingEntityAIStep() {
        handleEntityCollisions(player);

        SimpleCollisionBox oldBB = player.boundingBox.copy();

        if (!player.inVehicle()) {
            playerEntityTravel();
        } else {
            livingEntityTravel();
        }

        player.uncertaintyHandler.xNegativeUncertainty = 0;
        player.uncertaintyHandler.xPositiveUncertainty = 0;
        player.uncertaintyHandler.yNegativeUncertainty = 0;
        player.uncertaintyHandler.yPositiveUncertainty = 0;
        player.uncertaintyHandler.zNegativeUncertainty = 0;
        player.uncertaintyHandler.zPositiveUncertainty = 0;

        // A 1.8 player may spawn and get -0.1 gravity instead of -0.08 gravity
        if (player.uncertaintyHandler.lastTeleportTicks.hasOccurredSince(0)) {
            player.uncertaintyHandler.yNegativeUncertainty -= 0.02;
        }

        if (player.isFlying) {
            SimpleCollisionBox playerBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.lastX, player.lastY, player.lastZ);
            if (!Collisions.isEmpty(player, playerBox.copy().offset(0, 0.1, 0))) {
                player.uncertaintyHandler.yPositiveUncertainty = player.flySpeed * 5;
            }

            if (!Collisions.isEmpty(player, playerBox.copy().offset(0, -0.1, 0))) {
                player.uncertaintyHandler.yNegativeUncertainty = player.flySpeed * -5;
            }
        }
    }

    public void playerEntityTravel() {
        if (player.isFlying && !player.inVehicle()) {
            double oldY = player.clientVelocity.getY();
            double oldYJumping = oldY + player.flySpeed * 3;
            livingEntityTravel();

            if (player.predictedVelocity.isKnockback() || player.predictedVelocity.isTrident()
                    || player.uncertaintyHandler.yPositiveUncertainty != 0 || player.uncertaintyHandler.yNegativeUncertainty != 0 || player.isGliding) {
                player.clientVelocity.setY(player.actualMovement.getY() * 0.6);
            } else if (Math.abs(oldY - player.actualMovement.getY()) < (oldYJumping - player.actualMovement.getY())) {
                player.clientVelocity.setY(oldY * 0.6);
            } else {
                player.clientVelocity.setY(oldYJumping * 0.6);
            }

        } else {
            livingEntityTravel();
        }
    }

    public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
    }

    public void doLavaMove() {
    }

    public void doNormalMove(float blockFriction) {
    }

    public void livingEntityTravel() {
        double playerGravity = !player.inVehicle()
                ? player.compensatedEntities.self.getAttributeValue(Attributes.GRAVITY)
                : player.compensatedEntities.self.getRiding().getAttributeValue(Attributes.GRAVITY);

        boolean isFalling = player.actualMovement.getY() <= 0.0;
        if (isFalling && player.compensatedEntities.getSlowFallingAmplifier().isPresent()) {
            playerGravity = Math.min(playerGravity, 0.01);
            // Set fall distance to 0 if the player has slow falling
            player.fallDistance = 0;
        }

        player.gravity = playerGravity;

        float swimFriction;

        double lavaLevel = 0;
        if (canStandOnLava())
            lavaLevel = player.compensatedWorld.getLavaFluidLevelAt(GrimMath.floor(player.lastX), GrimMath.floor(player.lastY), GrimMath.floor(player.lastZ));

        if (player.wasTouchingWater && !player.isFlying) {
            // 0.8F seems hardcoded in
            // 1.13+ players on skeleton horses swim faster! Cool feature.
            boolean isSkeletonHorse = player.inVehicle() && player.compensatedEntities.self.getRiding().type == EntityTypes.SKELETON_HORSE;
            swimFriction = player.isSprinting ? 0.9F : isSkeletonHorse ? 0.96F : 0.8F;
            float swimSpeed = 0.02F;


            if (!player.lastOnGround) {
                player.depthStriderLevel *= 0.5F;
            }

            if (player.depthStriderLevel > 0.0F) {
                final float divisor = 1.0F;
                swimFriction += (0.54600006F - swimFriction) * player.depthStriderLevel / divisor;
                swimSpeed += (player.speed - swimSpeed) * player.depthStriderLevel / divisor;
            }

            if (player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.DOLPHINS_GRACE).isPresent()) {
                swimFriction = 0.96F;
            }

            player.friction = swimFriction; // Not vanilla, just useful for other grim stuff
            doWaterMove(swimSpeed, isFalling, swimFriction);

            player.isClimbing = Collisions.onClimbable(player, player.x, player.y, player.z);

            // 1.13 and below players can't climb ladders while touching water
            // yes, 1.13 players cannot climb ladders underwater
            if (player.isClimbing) {
                player.lastWasClimbing = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(player, playerGravity, isFalling, player.clientVelocity.clone().setY(0.2D * 0.8F)).getY();
            }

            floatInWaterWhileRidden();
        } else {
            if (player.wasTouchingLava && !player.isFlying && !(lavaLevel > 0 && canStandOnLava())) {
                player.friction = 0.5F; // Not vanilla, just useful for other grim stuff

                doLavaMove();

                // Lava movement changed in 1.16

                if (player.getFluidHeight(FluidTag.LAVA) <= 0.4D) {
                    player.clientVelocity = player.clientVelocity.multiply(0.5D, 0.800000011920929D, 0.5D);
                    player.clientVelocity = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(player, playerGravity, isFalling, player.clientVelocity);
                } else {
                    player.clientVelocity.multiply(0.5D);
                }

                if (player.hasGravity)
                    player.clientVelocity.add(0.0D, -playerGravity / 4.0D, 0.0D);

            } else if (player.isGliding) {
                if (Collisions.onClimbable(player, player.lastX, player.lastY, player.lastZ)) {
                    float blockFriction = BlockProperties.getFriction(player, player.mainSupportingBlockData, new Vector3d(player.lastX, player.lastY, player.lastZ));
                    float airDrag = getAirDrag(player);
                    float modifiedBlockFriction = getBlockFrictionModified(player, blockFriction);
                    player.friction = player.lastOnGround ? modifiedBlockFriction * airDrag : airDrag;

                    doNormalMove(blockFriction);

                    player.isGliding = false;
                    player.pointThreeEstimator.updatePlayerGliding(); // TODO: should this be true even if player stopped gliding?
                } else {
                    player.friction = 0.99F; // Not vanilla, just useful for other grim stuff
                    // Set fall distance to 1 if the player’s y velocity is greater than -0.5 when falling
                    if (player.clientVelocity.getY() > -0.5) {
                        player.fallDistance = 1;
                    }

                    new PredictionEngineElytra().guessBestMovement(0, player);
                }
            } else {
                float blockFriction = BlockProperties.getFriction(player, player.mainSupportingBlockData, new Vector3d(player.lastX, player.lastY, player.lastZ));
                float airDrag = getAirDrag(player);
                float modifiedBlockFriction = getBlockFrictionModified(player, blockFriction);
                player.friction = player.lastOnGround ? modifiedBlockFriction * airDrag : airDrag;

                doNormalMove(blockFriction);
            }
        }

        Collisions.applyEffectsFromBlocks(player);
    }

    private void floatInWaterWhileRidden() {
        if (!player.inVehicle()) return;

        PacketEntity vehicle = player.getVehicle();
        boolean canFloatWhileRidden = EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN.anyOf(vehicle.type);
        double fluidHeight = player.getFluidHeight(FluidTag.WATER);
        if (canFloatWhileRidden && player.inVehicle() && fluidHeight > 0.4) {
            player.clientVelocity.add(0.0, 0.04F, 0.0);
        }
    }

    public boolean canStandOnLava() {
        return false;
    }
}
