package ac.reaper.platform.fabric;

import ac.reaper.platform.api.PlatformServer;
import ac.reaper.platform.api.sender.Sender;
import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFabricPlatformServer implements PlatformServer {

    public PermissionLevel getOperatorPermissionLevel() {
        return ReaperACFabricLoaderPlugin.FABRIC_SERVER.operatorUserPermissions().level();
    }

    public boolean hasPermission(CommandSourceStack stack, PermissionLevel level) {
        return stack.permissions().hasPermission(new Permission.HasCommandLevel(level));
    }

    @Override
    public String getPlatformImplementationString() {
        // Return the Fabric server version
        return "Fabric " + FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString() + " (MC: " + ReaperACFabricLoaderPlugin.FABRIC_SERVER.getServerVersion() + ")";
    }

    @Override
    public Sender getConsoleSender() {
        CommandSourceStack consoleSource = ReaperACFabricLoaderPlugin.FABRIC_SERVER.createCommandSourceStack();
        return ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory().wrap(consoleSource);
    }

    @Override
    public void registerOutgoingPluginChannel(String name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public GameProfile getProfileByName(String name) {
        return ReaperACFabricLoaderPlugin.FABRIC_SERVER.services().profileResolver().fetchByName(name).orElse(null);
    }
}
