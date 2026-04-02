package ac.reaper.utils.data.packetentity;

import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.data.VectorData;

import java.util.Set;

public interface JumpableEntity {

    boolean isJumping();

    void setJumping(boolean jumping);

    float getJumpPower();

    void setJumpPower(float jumpPower);

    boolean canPlayerJump(ReaperPlayer player);

    boolean hasSaddle();

    void executeJump(ReaperPlayer player, Set<VectorData> possibleVectors);

}
