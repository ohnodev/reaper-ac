package ac.reaper.reaperac.platform.fabric.mc262;

import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.mc262.convert.Fabric262ConversionUtil;
import ac.reaper.reaperac.platform.fabric.mc262.convert.Fabric262MessageUtil;
import ac.reaper.reaperac.platform.fabric.mc262.entity.Fabric262GrimEntity;
import ac.reaper.reaperac.platform.fabric.mc262.player.Fabric262PlatformInventory;
import ac.reaper.reaperac.platform.fabric.mc262.player.Fabric262PlatformPlayer;
import ac.reaper.reaperac.platform.fabric.player.FabricPlatformPlayerFactory;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric262LoaderPlugin extends GrimACFabricLoaderPlugin {

    public GrimACFabric262LoaderPlugin() {
        super(
                new FabricPlatformPlayerFactory(
                        Fabric262PlatformPlayer::new,
                        Fabric262GrimEntity::new,
                        Fabric262PlatformInventory::new
                ),
                new Fabric262PlatformServer(),
                new Fabric262MessageUtil(),
                new Fabric262ConversionUtil()
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_26_2;
    }
}
