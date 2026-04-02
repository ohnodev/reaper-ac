package ac.reaper.platform.fabric.mc1194;

import ac.reaper.platform.fabric.AbstractFabricPlatformServer;
import ac.reaper.platform.api.manager.CommandAdapter;
import ac.reaper.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.reaper.platform.fabric.command.FabricPlayerSelectorParser;
import ac.reaper.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.reaper.platform.fabric.mc1171.ReaperACFabric1170LoaderPlugin;
import ac.reaper.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.reaper.platform.fabric.mc1194.convert.Fabric1190MessageUtil;
import ac.reaper.platform.fabric.mc1194.entity.Fabric1194ReaperEntity;
import ac.reaper.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.reaper.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.platform.fabric.utils.message.IFabricMessageUtil;
import ac.reaper.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;


public class ReaperACFabric1190LoaderPlugin extends ReaperACFabric1170LoaderPlugin {

    public ReaperACFabric1190LoaderPlugin() {
        this(
                () -> new FabricParserDescriptorFactory(
                    new FabricPlayerSelectorParser<>(Fabric1161PlayerSelectorAdapter::new)
            ),
            new FabricPlatformPlayerFactory(
                    Fabric1170PlatformPlayer::new,
                    Fabric1194ReaperEntity::new,
                    PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_19_2)
                            ? Fabric1193PlatformInventory::new : Fabric1161PlatformInventory::new
            ),
            new Fabric1190PlatformServer(),
            new Fabric1190MessageUtil(),
            new Fabric1140ConversionUtil()
        );
    }

    protected ReaperACFabric1190LoaderPlugin(
            LazyHolder<CommandAdapter> parserDescriptorFactory,
            FabricPlatformPlayerFactory platformPlayerFactory,
            AbstractFabricPlatformServer platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil) {
        super(parserDescriptorFactory, platformPlayerFactory, platformServer, fabricMessageUtil, fabricConversionUtil);
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_19_4;
    }
}
