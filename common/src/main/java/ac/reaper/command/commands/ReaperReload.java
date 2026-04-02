package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.utils.anticheat.MessageUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public class ReaperReload implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("reload")
                        .permission("reaper.reload")
                        .handler(this::handleReload)
        );
    }

    private void handleReload(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();

        // reload config
        sender.sendMessage(MessageUtil.getParsedComponent(sender, "reloading", "%prefix% &7Reloading config..."));

        ReaperAPI.INSTANCE.getExternalAPI().reloadAsync().exceptionally(throwable -> false)
                .thenAccept(bool -> {
                    Component message = bool
                            ? MessageUtil.getParsedComponent(sender, "reloaded", "%prefix% &fConfig has been reloaded.")
                            : MessageUtil.getParsedComponent(sender, "reload-failed", "%prefix% &cFailed to reload config.");
                    sender.sendMessage(message);
                });
    }
}
