package ac.grim.grimac.platform.fabric.mc261;

import ac.grim.grimac.platform.fabric.mc261.command.Fabric261PlayerSelectorAdapter;
import ac.grim.grimac.platform.fabric.command.FabricPlayerSelectorParser;
import ac.grim.grimac.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.grim.grimac.platform.fabric.mc1194.GrimACFabric1190LoaderPlugin;
import ac.grim.grimac.platform.fabric.mc1194.entity.Fabric1194GrimEntity;
import ac.grim.grimac.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.grim.grimac.platform.fabric.mc1205.Fabric1203PlatformServer;
import ac.grim.grimac.platform.fabric.mc1205.convert.Fabric1200MessageUtil;
import ac.grim.grimac.platform.fabric.mc1205.convert.Fabric1205ConversionUtil;
import ac.grim.grimac.platform.fabric.mc261.convert.Fabric261ConversionUtil;
import ac.grim.grimac.platform.fabric.mc261.player.Fabric261PlatformInventory;
import ac.grim.grimac.platform.fabric.mc261.player.Fabric261PlatformPlayer;
import ac.grim.grimac.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.grim.grimac.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

/**
 * Fabric entry for Minecraft 26.1+ servers. All 26.1-specific platform types live under {@code mc261}.
 */
public class GrimACFabric261LoaderPlugin extends GrimACFabric1190LoaderPlugin {

    public GrimACFabric261LoaderPlugin() {
        super(
                LazyHolder.simple(() -> new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric261PlayerSelectorAdapter::new)
                )),
                new FabricPlatformPlayerFactory(
                        Fabric261PlatformPlayer::new,
                        Fabric1194GrimEntity::new,
                        PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_4)
                                ? Fabric261PlatformInventory::new
                                : Fabric1193PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_10)
                        ? new Fabric261PlatformServer()
                        : new Fabric1203PlatformServer(),
                new Fabric1200MessageUtil(),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_5)
                        ? new Fabric261ConversionUtil()
                        : new Fabric1205ConversionUtil()
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_26_1;
    }
}
