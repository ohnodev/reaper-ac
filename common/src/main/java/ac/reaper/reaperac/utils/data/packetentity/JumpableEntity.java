package ac.reaper.reaperac.utils.data.packetentity;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.VectorData;

import java.util.Set;

public interface JumpableEntity {

    boolean isJumping();

    void setJumping(boolean jumping);

    float getJumpPower();

    void setJumpPower(float jumpPower);

    boolean canPlayerJump(GrimPlayer player);

    boolean hasSaddle();

    void executeJump(GrimPlayer player, Set<VectorData> possibleVectors);

}
