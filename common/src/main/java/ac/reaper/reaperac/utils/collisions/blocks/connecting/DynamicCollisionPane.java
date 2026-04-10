package ac.reaper.reaperac.utils.collisions.blocks.connecting;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.CollisionData;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionFactory;
import ac.reaper.reaperac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.enums.East;
import com.github.retrooper.packetevents.protocol.world.states.enums.North;
import com.github.retrooper.packetevents.protocol.world.states.enums.South;
import com.github.retrooper.packetevents.protocol.world.states.enums.West;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

public class DynamicCollisionPane extends DynamicConnecting implements CollisionFactory {

    private static final CollisionBox[] COLLISION_BOXES = makeShapes(1.0F, 1.0F, 16.0F, 0.0F, 16.0F, true, 1);

    @Override
    public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        boolean east;
        boolean north;
        boolean south;
        boolean west;

        // 1.13+ servers on 1.13+ clients send the full fence data

        east = block.getEast() != East.FALSE;
        north = block.getNorth() != North.FALSE;
        south = block.getSouth() != South.FALSE;
        west = block.getWest() != West.FALSE;

        // On 1.7 and 1.8 clients, and 1.13+ clients on 1.7 and 1.8 servers, the glass pane is + instead of |

        return COLLISION_BOXES[getAABBIndex(north, east, south, west)].copy();
    }

    @Override
    public boolean canConnectToGlassBlock() {
        return true;
    }

    @Override
    public boolean checkCanConnect(GrimPlayer player, WrappedBlockState state, StateType one, StateType two, BlockFace direction) {
        if (BlockTags.GLASS_PANES.contains(one) || one == StateTypes.IRON_BARS)
            return true;
        else {
            if (one == StateTypes.CHAIN) {
                player.getClientVersion();
            }
            return CollisionData.getData(one).getMovementCollisionBox(player, player.getClientVersion(), state, 0, 0, 0).isSideFullBlock(direction);
        }
    }
}
