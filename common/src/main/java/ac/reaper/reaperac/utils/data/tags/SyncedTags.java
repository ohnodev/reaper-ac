package ac.reaper.reaperac.utils.data.tags;

import ac.reaper.reaperac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTags;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This class stores tags that the client is aware of.
 */
public final class SyncedTags {

    public static final ResourceLocation CLIMBABLE = ResourceLocation.minecraft("climbable");
    public static final ResourceLocation MINEABLE_AXE = ResourceLocation.minecraft("mineable/axe");
    public static final ResourceLocation MINEABLE_PICKAXE = ResourceLocation.minecraft("mineable/pickaxe");
    public static final ResourceLocation MINEABLE_SHOVEL = ResourceLocation.minecraft("mineable/shovel");
    public static final ResourceLocation MINEABLE_HOE = ResourceLocation.minecraft("mineable/hoe");
    public static final ResourceLocation NEEDS_DIAMOND_TOOL = ResourceLocation.minecraft("needs_diamond_tool");
    public static final ResourceLocation NEEDS_IRON_TOOL = ResourceLocation.minecraft("needs_iron_tool");
    public static final ResourceLocation NEEDS_STONE_TOOL = ResourceLocation.minecraft("needs_stone_tool");
    public static final ResourceLocation SWORD_EFFICIENT = ResourceLocation.minecraft("sword_efficient");
    private static final ServerVersion VERSION = PacketEvents.getAPI().getServerManager().getVersion();
    private static final ResourceLocation BLOCK = ResourceLocation.minecraft("block");
    private final Map<ResourceLocation, Map<ResourceLocation, SyncedTag<?>>> synced;

    public SyncedTags(GrimPlayer player) {
        this.synced = new HashMap<>();
        ClientVersion version = player.getClientVersion();
        trackTags(BLOCK, id -> StateTypes.getById(VERSION.toClientVersion(), id),
                SyncedTag.<StateType>builder(CLIMBABLE).defaults(BlockTags.CLIMBABLE.getStates()).supported(true),
                SyncedTag.<StateType>builder(MINEABLE_AXE).defaults(BlockTags.MINEABLE_AXE.getStates()).supported(true),
                SyncedTag.<StateType>builder(MINEABLE_PICKAXE).defaults(BlockTags.MINEABLE_PICKAXE.getStates()).supported(true),
                SyncedTag.<StateType>builder(MINEABLE_SHOVEL).defaults(BlockTags.MINEABLE_SHOVEL.getStates()).supported(true),
                SyncedTag.<StateType>builder(MINEABLE_HOE).defaults(BlockTags.MINEABLE_HOE.getStates()).supported(true),
                SyncedTag.<StateType>builder(NEEDS_DIAMOND_TOOL).defaults(BlockTags.NEEDS_DIAMOND_TOOL.getStates()).supported(true),
                SyncedTag.<StateType>builder(NEEDS_IRON_TOOL).defaults(BlockTags.NEEDS_IRON_TOOL.getStates()).supported(true),
                SyncedTag.<StateType>builder(NEEDS_STONE_TOOL).defaults(BlockTags.NEEDS_STONE_TOOL.getStates()).supported(true),
                SyncedTag.<StateType>builder(SWORD_EFFICIENT).defaults(BlockTags.SWORD_EFFICIENT.getStates()).supported(true)
        );
    }

    @SafeVarargs
    private <T> void trackTags(ResourceLocation location, Function<Integer, T> remapper, SyncedTag.Builder<T>... syncedTags) {
        final Map<ResourceLocation, SyncedTag<?>> tags = new HashMap<>(syncedTags.length);
        for (SyncedTag.Builder<T> syncedTag : syncedTags) {
            syncedTag.remapper(remapper);
            final SyncedTag<T> built = syncedTag.build();
            tags.put(built.location(), built);
        }
        synced.put(location, tags);
    }

    public SyncedTag<StateType> block(ResourceLocation tag) {
        final Map<ResourceLocation, SyncedTag<?>> blockTags = synced.get(BLOCK);
        return (SyncedTag<StateType>) blockTags.get(tag);
    }

    public void handleTagSync(WrapperPlayServerTags tags) {
        tags.getTagMap().forEach((location, tagList) -> {
            if (!synced.containsKey(location)) return;
            final Map<ResourceLocation, SyncedTag<?>> syncedTags = synced.get(location);
            tagList.forEach(tag -> {
                if (!syncedTags.containsKey(tag.getKey())) return;
                syncedTags.get(tag.getKey()).readTagValues(tag);
            });
        });
    }
}
