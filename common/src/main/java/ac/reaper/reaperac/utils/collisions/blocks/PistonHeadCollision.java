package ac.reaper.reaperac.utils.collisions.blocks;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionFactory;
import ac.reaper.reaperac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.HexCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

public class PistonHeadCollision implements CollisionFactory {
    // 1.13+ clients are capable of seeing 1.13+ short pistons - we can look at block data to check
    @Override
    public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        // 1.13+ clients differentiate short and long, and the short vs long data is stored
        // Follow the server's version on 1.13+ clients, as that's the correct way to do it

        double longAmount = block.isShort() ? 0 : 4;

        return switch (block.getFacing()) {
            case UP -> new ComplexCollisionBox(2,
                    new HexCollisionBox(0, 12, 0, 16, 16, 16),
                    new HexCollisionBox(6, 0 - longAmount, 6, 10, 12, 10));
            case NORTH -> new ComplexCollisionBox(2,
                    new HexCollisionBox(0, 0, 0, 16, 16, 4),
                    new HexCollisionBox(6, 6, 4, 10, 10, 16 + longAmount));
            case SOUTH -> new ComplexCollisionBox(2,
                    new HexCollisionBox(0, 0, 12, 16, 16, 16),
                    new HexCollisionBox(6, 6, 0 - longAmount, 10, 10, 12));
            case WEST -> new ComplexCollisionBox(2,
                    new HexCollisionBox(0, 0, 0, 4, 16, 16),
                    new HexCollisionBox(4, 6, 6, 16 + longAmount, 10, 10));
            case EAST -> new ComplexCollisionBox(2,
                    new HexCollisionBox(12, 0, 0, 16, 16, 16),
                    new HexCollisionBox(0 - longAmount, 6, 4, 12, 10, 12));
            default -> new ComplexCollisionBox(2,
                    new HexCollisionBox(0, 0, 0, 16, 4, 16),
                    new HexCollisionBox(6, 4, 6, 10, 16 + longAmount, 10));
        };
    }
}
