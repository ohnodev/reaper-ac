package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public class ReaperVerbose implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("verbose")
                        .permission("reaper.verbose")
                        .handler(this::handleVerbose)
        );
    }

    private void handleVerbose(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        if (sender.isPlayer()) {
            ReaperAPI.INSTANCE.getAlertManager().toggleVerbose(context.sender().getPlatformPlayer(), false);
        } else if (sender.isConsole()) {
            ReaperAPI.INSTANCE.getAlertManager().toggleConsoleVerbose();
        }
    }
}
