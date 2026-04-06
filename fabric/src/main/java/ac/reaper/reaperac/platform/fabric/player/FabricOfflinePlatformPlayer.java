package ac.reaper.reaperac.platform.fabric.player;

import ac.reaper.reaperac.platform.api.player.OfflinePlatformPlayer;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class FabricOfflinePlatformPlayer implements OfflinePlatformPlayer {
    private final @NotNull UUID uniqueId;
    private final @NotNull String name;

    @Override
    public boolean isOnline() {
        return GrimACFabricLoaderPlugin.FABRIC_SERVER.getPlayerList().getPlayer(uniqueId) != null;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OfflinePlatformPlayer player && this.getUniqueId().equals(player.getUniqueId());
    }
}
