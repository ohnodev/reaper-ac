package io.github.retrooper.packetevents;

import com.github.retrooper.packetevents.manager.registry.ItemRegistry;
import com.github.retrooper.packetevents.manager.registry.RegistryManager;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.mappings.IRegistry;
import com.github.retrooper.packetevents.util.mappings.SynchronizedRegistriesHandler;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsAPI;
import io.github.retrooper.packetevents.factory.fabric.FabricPlayerManager;
import io.github.retrooper.packetevents.loader.ChainLoadData;
import io.github.retrooper.packetevents.loader.ChainLoadEntryPoint;
import io.github.retrooper.packetevents.manager.AbstractFabricPlayerManager;
import io.github.retrooper.packetevents.manager.registry.FabricRegistryManager;
import io.github.retrooper.packetevents.util.LazyHolder;
import org.jetbrains.annotations.Nullable;

public class FabricOfficialChainLoadEntrypoint implements ChainLoadEntryPoint {
    private static final ClientVersion TARGET_CLIENT_VERSION = ServerVersion.V_26_2.toClientVersion();

    protected LazyHolder<AbstractFabricPlayerManager> playerManagerHolder =
            LazyHolder.simple(() -> new FabricPlayerManager());
    protected LazyHolder<RegistryManager> registryManagerHolder =
            LazyHolder.simple(() -> new FabricRegistryManager(new ItemRegistry() {
                @Override
                public @Nullable ItemType getByName(String name) {
                    IRegistry<ItemType> registry = resolveSyncedItemRegistry(TARGET_CLIENT_VERSION);
                    return registry != null
                            ? registry.getByName(TARGET_CLIENT_VERSION, name)
                            // Keep strict ItemTypes runtime registry semantics; do not bypass with baked registry access.
                            : ItemTypes.getByName(TARGET_CLIENT_VERSION, name);
                }

                @Override
                public @Nullable ItemType getById(int id) {
                    IRegistry<ItemType> registry = resolveSyncedItemRegistry(TARGET_CLIENT_VERSION);
                    return registry != null
                            ? registry.getById(TARGET_CLIENT_VERSION, id)
                            // Keep strict ItemTypes runtime registry semantics; do not bypass with baked registry access.
                            : ItemTypes.getById(TARGET_CLIENT_VERSION, id);
                }
            }));

    @Override
    public void initialize(ChainLoadData chainLoadData) {
        chainLoadData.setPlayerManagerIfNull(playerManagerHolder);
        chainLoadData.setClientPlayerManagerIfNull(playerManagerHolder);
        chainLoadData.setRegistryManagerIfNull(registryManagerHolder);
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_26_2;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable IRegistry<ItemType> resolveSyncedItemRegistry(ClientVersion version) {
        SynchronizedRegistriesHandler.RegistryEntry<?> entry =
                SynchronizedRegistriesHandler.getRegistryEntry(ItemTypes.getRegistry().getRegistryKey());
        if (entry == null) {
            return null;
        }
        return (IRegistry<ItemType>) entry.getSyncedRegistry(version);
    }
}
