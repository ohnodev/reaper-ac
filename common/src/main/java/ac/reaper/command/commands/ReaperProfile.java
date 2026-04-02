package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.command.PlayerSelector;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.player.PlatformPlayer;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.MessageUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReaperProfile implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("profile")
                        .permission("reaper.profile")
                        .required("target", adapter.singlePlayerSelectorParser())
                        .handler(this::handleProfile)
        );
    }

    private void handleProfile(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        PlayerSelector target = context.get("target");

        PlatformPlayer targetPlatformPlayer = target.getSinglePlayer().getPlatformPlayer();
        if (Objects.requireNonNull(targetPlatformPlayer).isExternalPlayer()) {
            sender.sendMessage(MessageUtil.getParsedComponent(sender,"player-not-this-server", "%prefix% &cThis player isn't on this server!"));
            return;
        }

        ReaperPlayer reaperPlayer = ReaperAPI.INSTANCE.getPlayerDataManager().getPlayer(targetPlatformPlayer.getUniqueId());
        if (reaperPlayer == null) {
            sender.sendMessage(MessageUtil.getParsedComponent(sender, "player-not-found", "%prefix% &cPlayer is exempt or offline!"));
            return;
        }

        for (String message : ReaperAPI.INSTANCE.getConfigManager().getConfig().getStringList("profile")) {
            final Component component = MessageUtil.miniMessage(message);
            sender.sendMessage(MessageUtil.replacePlaceholders(reaperPlayer, component));
        }
    }
}
