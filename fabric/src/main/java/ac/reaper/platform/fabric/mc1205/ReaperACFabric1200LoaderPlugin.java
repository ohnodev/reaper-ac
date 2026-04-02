package ac.reaper.platform.fabric.mc1205;

import ac.reaper.platform.fabric.command.FabricPlayerSelectorParser;
import ac.reaper.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.reaper.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.reaper.platform.fabric.mc1194.Fabric1190PlatformServer;
import ac.reaper.platform.fabric.mc1194.ReaperACFabric1190LoaderPlugin;
import ac.reaper.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.reaper.platform.fabric.mc1205.convert.Fabric1200MessageUtil;
import ac.reaper.platform.fabric.mc1205.convert.Fabric1205ConversionUtil;
import ac.reaper.platform.fabric.mc1194.entity.Fabric1194ReaperEntity;
import ac.reaper.platform.fabric.mc1205.player.Fabric1202PlatformPlayer;
import ac.reaper.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.reaper.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.reaper.platform.fabric.player.FabricPlatformPlayerFactory;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class ReaperACFabric1200LoaderPlugin extends ReaperACFabric1190LoaderPlugin {

    public ReaperACFabric1200LoaderPlugin() {
        super(
                () -> new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric1161PlayerSelectorAdapter::new)
                ),
                new FabricPlatformPlayerFactory(
                        PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_20_1)
                                ? Fabric1202PlatformPlayer::new : Fabric1170PlatformPlayer::new,
                        Fabric1194ReaperEntity::new,
                        Fabric1193PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_20_2)
                        ? new Fabric1203PlatformServer() : new Fabric1190PlatformServer(),
                new Fabric1200MessageUtil(),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_20_4)
                        ? new Fabric1205ConversionUtil() : new Fabric1140ConversionUtil()
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_20_5;
    }
}
