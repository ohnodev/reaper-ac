package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReaperAlerts implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("alerts", Description.of("Toggle alerts for the sender"))
                        .permission("reaper.alerts")
                        .handler(this::handleAlerts)
        );
    }

    // Suppress warning as we've already checked sender is not console
    private void handleAlerts(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        if (sender.isPlayer()) {
            ReaperAPI.INSTANCE.getAlertManager().toggleAlerts(Objects.requireNonNull(context.sender().getPlatformPlayer()), false);
        } else if (sender.isConsole()) {
            ReaperAPI.INSTANCE.getAlertManager().toggleConsoleAlerts();
        }
    }
}
