package ac.reaper.platform.fabric.mc1161;

import ac.reaper.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.command.FabricPlayerSelectorParser;
import ac.reaper.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.reaper.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.reaper.platform.fabric.mc1161.entity.Fabric1161ReaperEntity;
import ac.reaper.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.reaper.platform.fabric.mc1161.player.Fabric1161PlatformPlayer;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.reaper.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.platform.fabric.utils.message.IFabricMessageUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class ReaperACFabric1161LoaderPlugin extends ReaperACFabricLoaderPlugin {

    public ReaperACFabric1161LoaderPlugin() {
        this(
            new FabricPlatformPlayerFactory(
                Fabric1161PlatformPlayer::new,
                Fabric1161ReaperEntity::new,
                Fabric1161PlatformInventory::new
            ),
            new Fabric1140PlatformServer(),
            new Fabric1161MessageUtil(),
            new Fabric1140ConversionUtil()
        );
    }

    protected ReaperACFabric1161LoaderPlugin(
            FabricPlatformPlayerFactory playerFactory,
            AbstractFabricPlatformServer platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil
    ) {
        super(() -> new FabricParserDescriptorFactory(new FabricPlayerSelectorParser<>(Fabric1161PlayerSelectorAdapter::new)),
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
