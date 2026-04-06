package ac.reaper.reaperac.platform.fabric.mc1161;

import ac.reaper.reaperac.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.mc1161.entity.Fabric1161GrimEntity;
import ac.reaper.reaperac.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.reaper.reaperac.platform.fabric.mc1161.player.Fabric1161PlatformPlayer;
import ac.reaper.reaperac.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.reaperac.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.reaper.reaperac.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.reaperac.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.reaperac.platform.fabric.utils.message.IFabricMessageUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric1161LoaderPlugin extends GrimACFabricLoaderPlugin {

    public GrimACFabric1161LoaderPlugin() {
        this(
            new FabricPlatformPlayerFactory(
                Fabric1161PlatformPlayer::new,
                Fabric1161GrimEntity::new,
                Fabric1161PlatformInventory::new
            ),
            new Fabric1140PlatformServer(),
            new Fabric1161MessageUtil(),
            new Fabric1140ConversionUtil()
        );
    }

    protected GrimACFabric1161LoaderPlugin(
            FabricPlatformPlayerFactory playerFactory,
            AbstractFabricPlatformServer platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil
    ) {
        super(
            playerFactory,
            platformServer,
            fabricMessageUtil,
            fabricConversionUtil
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_16_1;
    }
}
