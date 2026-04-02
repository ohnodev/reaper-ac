package ac.reaper.platform.fabric.mixins;

import ac.reaper.platform.api.world.PlatformChunk;
import ac.reaper.platform.api.world.PlatformWorld;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;

@Mixin(Level.class)
@Implements(@Interface(iface = PlatformWorld.class, prefix = "reaperac$"))
abstract class LevelMixin implements LevelAccessor {

    @Shadow
    public abstract ResourceKey<Level> dimension();

    public boolean reaperac$isChunkLoaded(int chunkX, int chunkZ) {
        return hasChunk(chunkX, chunkZ);
    }

    public WrappedBlockState reaperac$getBlockAt(int x, int y, int z) {
        return WrappedBlockState.getByGlobalId(
                Block.getId(getBlockState(new BlockPos(x, y, z)))
        );
    }

    public String reaperac$getName() {
        return this.dimension().identifier().toString();
    }

    public @Nullable UUID reaperac$getUID() {
        throw new UnsupportedOperationException();
    }

    public PlatformChunk reaperac$getChunkAt(int currChunkX, int currChunkZ) {
        return (PlatformChunk) getChunk(currChunkX, currChunkZ);
    }

    public boolean reaperac$isLoaded() {
        return ReaperACFabricLoaderPlugin.FABRIC_SERVER.getLevel(this.dimension()) != null;
    }
}
