package ac.reaper.platform.fabric.mc1171;

import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.mc1161.Fabric1140PlatformServer;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Fabric1171PlatformServer extends Fabric1140PlatformServer {
    @Override
    public @Nullable GameProfile getProfileByName(String name) {
        Optional<GameProfile> gameProfile = ReaperACFabricLoaderPlugin.FABRIC_SERVER.getProfileCache().get(name);
        return gameProfile.orElse(null);
    }
}
