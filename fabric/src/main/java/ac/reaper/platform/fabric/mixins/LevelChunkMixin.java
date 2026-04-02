package ac.reaper.platform.fabric.mixins;

import ac.reaper.platform.api.world.PlatformChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelChunk.class)
@Implements(@Interface(iface = PlatformChunk.class, prefix = "reaperac$"))
abstract class LevelChunkMixin {
    // TODO (Fabric) (Region Threading) use ThreadLocal for Fabric region threading mods instead of a single variable
    // Having a single reaperac$sharedPos works when the server is run on the single thread, as vanilla does
    @Unique
    private static final BlockPos.MutableBlockPos reaperac$sharedPos = new BlockPos.MutableBlockPos();

    public int reaperac$getBlockID(int x, int y, int z) {
        LevelChunk chunk = (LevelChunk) (Object) this;
        reaperac$sharedPos.set(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
        return Block.getId(chunk.getBlockState(reaperac$sharedPos));
    }
}
