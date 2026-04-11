package ac.reaper.reaperac.utils.collisions.blocks;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionFactory;
import ac.reaper.reaperac.utils.collisions.datatypes.HexCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Type;

public class DynamicChest implements CollisionFactory {
    public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState chest, int x, int y, int z) {

        if (chest.getTypeData() == Type.SINGLE) {
            return new HexCollisionBox(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
        }

        if (chest.getFacing() == BlockFace.SOUTH && chest.getTypeData() == Type.RIGHT ||
                chest.getFacing() == BlockFace.NORTH && chest.getTypeData() == Type.LEFT) {
            return new HexCollisionBox(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D); // Connected to the east face
        } else if (chest.getFacing() == BlockFace.SOUTH && chest.getTypeData() == Type.LEFT ||
                chest.getFacing() == BlockFace.NORTH && chest.getTypeData() == Type.RIGHT) {
            return new HexCollisionBox(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D); // Connected to the west face
        } else if (
                chest.getFacing() == BlockFace.WEST && chest.getTypeData() == Type.RIGHT ||
                        chest.getFacing() == BlockFace.EAST && chest.getTypeData() == Type.LEFT) {
            return new HexCollisionBox(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D); // Connected to the south face
        } else {
            return new HexCollisionBox(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D); // Connected to the north face
        }
    }
}
