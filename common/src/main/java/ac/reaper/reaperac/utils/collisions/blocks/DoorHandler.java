package ac.reaper.reaperac.utils.collisions.blocks;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionFactory;
import ac.reaper.reaperac.utils.collisions.datatypes.HexCollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.NoCollisionBox;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.enums.Hinge;

public class DoorHandler implements CollisionFactory {
    protected static final CollisionBox SOUTH_AABB = new HexCollisionBox(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final CollisionBox NORTH_AABB = new HexCollisionBox(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final CollisionBox WEST_AABB = new HexCollisionBox(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final CollisionBox EAST_AABB = new HexCollisionBox(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    @Override
    public CollisionBox fetch(GrimPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z) {
        return switch (fetchDirection(player, version, block, x, y, z)) {
            case NORTH -> NORTH_AABB.copy();
            case SOUTH -> SOUTH_AABB.copy();
            case EAST -> EAST_AABB.copy();
            case WEST -> WEST_AABB.copy();
            default -> NoCollisionBox.INSTANCE;
        };

    }

    public BlockFace fetchDirection(GrimPlayer player, ClientVersion version, WrappedBlockState door, int x, int y, int z) {
        BlockFace facingDirection;
        boolean isClosed;
        boolean isRightHinge;

        //TODO: This needs to be updated to support corrupted door collision

        facingDirection = door.getFacing();
        isClosed = !door.isOpen();
        isRightHinge = door.getHinge() == Hinge.RIGHT;

        return switch (facingDirection) {
            case SOUTH ->
                    isClosed ? BlockFace.SOUTH : (isRightHinge ? BlockFace.EAST : BlockFace.WEST);
            case WEST ->
                    isClosed ? BlockFace.WEST : (isRightHinge ? BlockFace.SOUTH : BlockFace.NORTH);
            case NORTH ->
                    isClosed ? BlockFace.NORTH : (isRightHinge ? BlockFace.WEST : BlockFace.EAST);
            default ->
                    isClosed ? BlockFace.EAST : (isRightHinge ? BlockFace.NORTH : BlockFace.SOUTH);
        };
    }
}
