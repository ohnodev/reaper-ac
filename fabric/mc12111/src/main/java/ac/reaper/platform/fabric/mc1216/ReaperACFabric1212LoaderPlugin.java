package ac.reaper.platform.fabric.mc1216;

import ac.reaper.platform.fabric.mc1216.command.Fabric1212PlayerSelectorAdapter;
import ac.reaper.platform.fabric.command.FabricPlayerSelectorParser;
import ac.reaper.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.reaper.platform.fabric.mc1194.ReaperACFabric1190LoaderPlugin;
import ac.reaper.platform.fabric.mc1194.entity.Fabric1194ReaperEntity;
import ac.reaper.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.reaper.platform.fabric.mc1205.Fabric1203PlatformServer;
import ac.reaper.platform.fabric.mc1205.convert.Fabric1200MessageUtil;
import ac.reaper.platform.fabric.mc1205.convert.Fabric1205ConversionUtil;
import ac.reaper.platform.fabric.mc1216.convert.Fabric1216ConversionUtil;
import ac.reaper.platform.fabric.mc1216.player.Fabric1212PlatformPlayer;
import ac.reaper.platform.fabric.mc1216.player.Fabric1215PlatformInventory;
import ac.reaper.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.reaper.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class ReaperACFabric1212LoaderPlugin extends ReaperACFabric1190LoaderPlugin {

    public ReaperACFabric1212LoaderPlugin() {
        super(
                LazyHolder.simple(() -> new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric1212PlayerSelectorAdapter::new)
                )),
                new FabricPlatformPlayerFactory(
                        Fabric1212PlatformPlayer::new,
                        Fabric1194ReaperEntity::new,
                        PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_4)
                            ? Fabric1215PlatformInventory::new : Fabric1193PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_10) ?
                        new Fabric12111PlatformServer() : new Fabric1203PlatformServer(),
                new Fabric1200MessageUtil(),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_5)
                        ? new Fabric1216ConversionUtil() : new Fabric1205ConversionUtil()
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_21_11;
    }
}
