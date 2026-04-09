package ac.reaper.reaperac.checks.impl.breaking;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.PacketStateData;
import ac.reaper.reaperac.utils.data.packetentity.PacketEntitySelf;
import ac.reaper.reaperac.utils.data.tags.SyncedTag;
import ac.reaper.reaperac.utils.data.tags.SyncedTags;
import ac.reaper.reaperac.utils.enums.FluidTag;
import ac.reaper.reaperac.utils.latency.CompensatedEntities;
import ac.reaper.reaperac.utils.latency.CompensatedInventory;
import ac.reaper.reaperac.utils.nmsutil.BlockBreakSpeed;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.protocol.attribute.Attribute;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import io.github.retrooper.packetevents.impl.netty.NettyManagerImpl;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Builds a minimal mock {@link GrimPlayer} that satisfies the dependency chain for
 * {@link BlockBreakSpeed#getBlockDamage} without starting a real server or
 * initializing PacketEvents' heavy static registries.
 *
 * <p>All PE types ({@link ItemStack}, {@link StateType}) are mocked to avoid triggering
 * PE's static initializers that require a full runtime (netty, adventure-nbt, etc.).
 *
 * <p>Defaults: survival mode, on ground, no potions, no enchantments, not in water.
 */
public final class BlockBreakTestFixture implements AutoCloseable {

    private final MockedStatic<PacketEvents> packetEventsMock;
    private final GrimPlayer player;

    @SuppressWarnings("unchecked")
    public BlockBreakTestFixture() {
        packetEventsMock = mockStatic(PacketEvents.class);
        PacketEventsAPI<?> api = mock(PacketEventsAPI.class, RETURNS_DEEP_STUBS);
        packetEventsMock.when(PacketEvents::getAPI).thenReturn(api);

        ServerManager serverManager = mock(ServerManager.class);
        when(api.getServerManager()).thenReturn(serverManager);
        when(serverManager.getVersion()).thenReturn(ServerVersion.V_1_21_4);

        PacketEventsSettings settings = new PacketEventsSettings();
        when(api.getSettings()).thenReturn(settings);

        NettyManager nettyManager = new NettyManagerImpl();
        when(api.getNettyManager()).thenReturn(nettyManager);

        player = mock(GrimPlayer.class, RETURNS_DEEP_STUBS);
        when(player.getClientVersion()).thenReturn(ClientVersion.V_1_21_4);
        when(player.getTransactionPing()).thenReturn(100);

        player.gamemode = GameMode.SURVIVAL;
        setFinalField(player, "packetStateData", new PacketStateData());
        player.packetStateData.packetPlayerOnGround = true;

        stubCompensatedEntities();
        stubTagManager();
        stubInventoryEmpty();
        player.platformPlayer = null;
    }

    private void stubCompensatedEntities() {
        CompensatedEntities compensatedEntities = mock(CompensatedEntities.class, RETURNS_DEEP_STUBS);
        setFinalField(player, "compensatedEntities", compensatedEntities);

        PacketEntitySelf self = mock(PacketEntitySelf.class, RETURNS_DEEP_STUBS);
        compensatedEntities.self = self;

        when(self.getAttributeValue(any(Attribute.class))).thenAnswer(invocation -> {
            Attribute attr = invocation.getArgument(0);
            String name = attr != null ? attr.getName().getKey() : "";
            return switch (name) {
                case "block_break_speed" -> 1.0;
                case "submerged_mining_speed" -> 0.2;
                default -> 0.0; // mining_efficiency, etc.
            };
        });

        when(compensatedEntities.getPotionLevelForSelfPlayer(any())).thenReturn(OptionalInt.empty());
        when(player.isEyeInFluid(FluidTag.WATER)).thenReturn(false);
    }

    private void stubTagManager() {
        SyncedTags tagManager = mock(SyncedTags.class, RETURNS_DEEP_STUBS);
        setFinalField(player, "tagManager", tagManager);
        when(tagManager.block(any(ResourceLocation.class))).thenReturn(null);
    }

    /**
     * Configure tag manager to return a synced tag for the given resource location
     * that matches the given block types by name.
     */
    public void addSyncedBlockTag(ResourceLocation tagLoc, Set<String> blockNames) {
        @SuppressWarnings("unchecked")
        SyncedTag<StateType> tag = mock(SyncedTag.class);
        when(tag.matchesBlock(any(StateType.class))).thenAnswer(inv -> {
            StateType block = inv.getArgument(0);
            return blockNames.contains(block.getName());
        });
        when(player.tagManager.block(tagLoc)).thenReturn(tag);
    }

    public void setHeldItem(ItemStack tool) {
        CompensatedInventory inv = mock(CompensatedInventory.class);
        when(inv.getHeldItem()).thenReturn(tool);
        setFinalField(player, "inventory", inv);
        player.platformPlayer = null;
    }

    private void stubInventoryEmpty() {
        ItemStack empty = mock(ItemStack.class);
        when(empty.isEmpty()).thenReturn(true);
        when(empty.getType()).thenReturn(mock(ItemType.class));
        setHeldItem(empty);
    }

    public void setOnGround(boolean onGround) {
        player.packetStateData.packetPlayerOnGround = onGround;
    }

    public void setGameMode(GameMode mode) {
        player.gamemode = mode;
    }

    public GrimPlayer getPlayer() {
        return player;
    }

    /**
     * Compute block damage per tick using the real {@link BlockBreakSpeed} code.
     */
    public double computeBlockDamage(ItemStack tool, StateType block) {
        return BlockBreakSpeed.getBlockDamage(player, tool, block);
    }

    /**
     * Compute predicted break time in ms using FastBreak's formula:
     * {@code ceil(1 / damage) * 50}.
     */
    public double predictedBreakTimeMs(ItemStack tool, StateType block) {
        double damage = computeBlockDamage(tool, block);
        if (damage <= 0) return Double.MAX_VALUE;
        return Math.ceil(1.0 / damage) * 50.0;
    }

    @Override
    public void close() {
        packetEventsMock.close();
    }

    static void setFinalField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            Field field = null;
            while (clazz != null && field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field == null) {
                throw new NoSuchFieldException(fieldName + " not found in " + target.getClass());
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
