package ac.reaper.platform.fabric.mc1171;

import ac.reaper.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.platform.api.manager.CommandAdapter;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.command.FabricPlayerSelectorParser;
import ac.reaper.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.reaper.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.reaper.platform.fabric.mc1161.Fabric1140PlatformServer;
import ac.reaper.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.reaper.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.reaper.platform.fabric.mc1171.entity.Fabric1170ReaperEntity;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.reaper.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.platform.fabric.utils.message.IFabricMessageUtil;
import ac.reaper.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;


public class ReaperACFabric1170LoaderPlugin extends ReaperACFabricLoaderPlugin {

    public ReaperACFabric1170LoaderPlugin() {
        this(() -> new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric1161PlayerSelectorAdapter::new)
                ),
                new FabricPlatformPlayerFactory(
                        Fabric1170PlatformPlayer::new,
                        Fabric1170ReaperEntity::new,
                        Fabric1161PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_17)
                        ? new Fabric1171PlatformServer() : new Fabric1140PlatformServer(),
                new Fabric1161MessageUtil(),
                new Fabric1140ConversionUtil()
        );
    }

    protected ReaperACFabric1170LoaderPlugin(LazyHolder<CommandAdapter> parserDescriptorFactory,
                                           FabricPlatformPlayerFactory playerFactory,
                                           AbstractFabricPlatformServer platformServer,
                                           IFabricMessageUtil fabricMessageUtil,
                                           IFabricConversionUtil fabricConversionUtil) {
        super(
                parserDescriptorFactory,
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
