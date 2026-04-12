package ac.reaper.reaperac.predictionengine.movementtick;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntityHorse;
import ac.reaper.reaperac.utils.math.Vector3dm;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;

public class MovementTickerHorse extends MovementTickerLivingVehicle {

    public MovementTickerHorse(GrimPlayer player) {
        super(player);

        PacketEntityHorse horsePacket = (PacketEntityHorse) player.compensatedEntities.self.getRiding();
        if (!horsePacket.hasSaddle()) return;

        player.speed = (float) horsePacket.getAttributeValue(Attributes.MOVEMENT_SPEED) + getExtraSpeed();

        // Setup player inputs
        float horizInput = player.vehicleData.vehicleHorizontal * 0.5F;
        float forwardsInput = player.vehicleData.vehicleForward;

        if (forwardsInput <= 0.0F) {
            forwardsInput *= 0.25F;
        }

        this.movementInput = new Vector3dm(horizInput, 0, forwardsInput);
        if (this.movementInput.lengthSquared() > 1) this.movementInput.normalize();
    }

    public float getExtraSpeed() {
        return 0f;
    }
}
