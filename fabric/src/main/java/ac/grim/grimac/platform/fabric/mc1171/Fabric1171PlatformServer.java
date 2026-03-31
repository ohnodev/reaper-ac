package ac.grim.grimac.platform.fabric.mc1171;

import ac.grim.grimac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.grim.grimac.platform.fabric.mc1161.Fabric1140PlatformServer;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

public class Fabric1171PlatformServer extends Fabric1140PlatformServer {
    @Override
    public @Nullable GameProfile getProfileByName(String name) {
        return GrimACFabricLoaderPlugin.FABRIC_SERVER.services().profileResolver().fetchByName(name).orElse(null);
    }
}
