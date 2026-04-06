package ac.reaper.reaperac.platform.fabric.mc1171;

import ac.reaper.reaperac.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.reaper.reaperac.platform.fabric.mc1161.Fabric1140PlatformServer;
import ac.reaper.reaperac.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.reaper.reaperac.platform.fabric.mc1171.entity.Fabric1170GrimEntity;
import ac.reaper.reaperac.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.reaperac.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.reaper.reaperac.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.reaperac.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.reaperac.platform.fabric.utils.message.IFabricMessageUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;


public class GrimACFabric1170LoaderPlugin extends GrimACFabricLoaderPlugin {

    public GrimACFabric1170LoaderPlugin() {
        this(
                new FabricPlatformPlayerFactory(
                        Fabric1170PlatformPlayer::new,
                        Fabric1170GrimEntity::new,
                        Fabric1161PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_17)
                        ? new Fabric1171PlatformServer() : new Fabric1140PlatformServer(),
                new Fabric1161MessageUtil(),
                new Fabric1140ConversionUtil()
        );
    }

    protected GrimACFabric1170LoaderPlugin(FabricPlatformPlayerFactory playerFactory,
                                           AbstractFabricPlatformServer platformServer,
                                           IFabricMessageUtil fabricMessageUtil,
                                           IFabricConversionUtil fabricConversionUtil) {
        super(
                playerFactory,
                platformServer,
                fabricMessageUtil,
                fabricConversionUtil
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_17_1;
    }
}
