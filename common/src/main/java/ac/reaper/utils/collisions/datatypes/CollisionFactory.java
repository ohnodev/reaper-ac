package ac.reaper.utils.collisions.datatypes;

import ac.reaper.player.ReaperPlayer;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

public interface CollisionFactory {
    CollisionBox fetch(ReaperPlayer player, ClientVersion version, WrappedBlockState block, int x, int y, int z);
}
