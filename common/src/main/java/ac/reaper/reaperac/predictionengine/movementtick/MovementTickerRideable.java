package ac.reaper.reaperac.predictionengine.movementtick;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityRideable;
import ac.reaper.reaperac.utils.nmsutil.Collisions;

public class MovementTickerRideable extends MovementTickerLivingVehicle {

    public MovementTickerRideable(GrimPlayer player) {
        super(player);

        // If the player has carrot/fungus on a stick, otherwise the player has no control
        float f = getSteeringSpeed();

        PacketEntityRideable boost = ((PacketEntityRideable) player.compensatedEntities.self.getRiding());

        // Do stuff for boosting on a pig/strider
        if (boost.currentBoostTime++ < boost.boostTimeMax) {
            // I wonder how much fastmath actually affects boosting movement
            f += f * 1.15F * player.trigHandler.sin((float) boost.currentBoostTime / (float) boost.boostTimeMax * (float) Math.PI);
        }

        player.speed = f;

    }

    // Pig and Strider should implement this
    public float getSteeringSpeed() {
        throw new IllegalStateException("Not implemented");
    }

}
