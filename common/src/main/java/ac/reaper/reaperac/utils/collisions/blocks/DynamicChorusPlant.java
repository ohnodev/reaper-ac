package ac.reaper.reaperac.utils.collisions.blocks;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionFactory;
import ac.reaper.reaperac.utils.collisions.datatypes.ComplexCollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.East;
import com.github.retrooper.packetevents.protocol.world.states.enums.North;
import com.github.retrooper.packetevents.protocol.world.states.enums.South;
import com.github.retrooper.packetevents.protocol.world.states.enums.West;

import java.util.HashSet;
import java.util.Set;

// 1.13 clients on 1.13 servers get everything included in the block data, no world reading required
public class DynamicChorusPlant implements CollisionFactory {
    private static final BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
    private static final CollisionBox[] modernShapes = makeShapes();

    private static CollisionBox[] makeShapes() {
        float f = 0.5F - (float) 0.3125;
        float f1 = 0.5F + (float) 0.3125;
        SimpleCollisionBox baseShape = new SimpleCollisionBox(f, f, f, f1, f1, f1, false);
        SimpleCollisionBox[] avoxelshape = new SimpleCollisionBox[directions.length];

        for (int i = 0; i < directions.length; ++i) {
            BlockFace direction = directions[i];
            avoxelshape[i] = new SimpleCollisionBox(0.5D + Math.min(-(float) 0.3125, (double) direction.getModX() * 0.5D), 0.5D + Math.min(-(float) 0.3125, (double) direction.getModY() * 0.5D), 0.5D + Math.min(-(float) 0.3125, (double) direction.getModZ() * 0.5D), 0.5D + Math.max((float) 0.3125, (double) direction.getModX() * 0.5D), 0.5D + Math.max((float) 0.3125, (double) direction.getModY() * 0.5D), 0.5D + Math.max((float) 0.3125, (double) direction.getModZ() * 0.5D), false);
        }

        CollisionBox[] avoxelshape1 = new CollisionBox[64];

        for (int k = 0; k < 64; ++k) {
            ComplexCollisionBox directionalShape = new ComplexCollisionBox(7, baseShape); // how big is this one??

            for (int j = 0; j < directions.length; ++j) {
                if ((k & 1 << j) != 0) {
                    directionalShape.add(avoxelshape[j]);
                }
            }

            avoxelshape1[k] = directionalShape;
        }

        return avoxelshape1;
    }

    @Override
    public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        Set<BlockFace> directions = new HashSet<>();

        if (block.getWest() == West.TRUE) directions.add(BlockFace.WEST);
        if (block.getEast() == East.TRUE) directions.add(BlockFace.EAST);
        if (block.getNorth() == North.TRUE) directions.add(BlockFace.NORTH);
        if (block.getSouth() == South.TRUE) directions.add(BlockFace.SOUTH);
        if (block.isUp()) directions.add(BlockFace.UP);
        if (block.isDown()) directions.add(BlockFace.DOWN);
        // Player is 1.13+ on 1.13+ server
        return modernShapes[getAABBIndex(directions)].copy();
    }

    protected int getAABBIndex(Set<BlockFace> p_196486_1_) {
        int i = 0;

        for (int j = 0; j < directions.length; ++j) {
            if (p_196486_1_.contains(directions[j])) {
                i |= 1 << j;
            }
        }

        return i;
    }
}
