package ac.reaper.command.commands;

import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.predictionengine.MovementCheckRunner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public class ReaperPerf {

    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        Command.Builder<Sender> reaperCommand = commandManager.commandBuilder("reaper", "reaperac");

        Command.Builder<Sender> configuredBuilder = reaperCommand
                .literal("perf", "performance")
                .permission("reaper.performance")
                .handler(this::handlePerformance);

        commandManager.command(configuredBuilder);
    }

    private void handlePerformance(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();

        double millis = MovementCheckRunner.predictionNanos / 1000000;
        double longMillis = MovementCheckRunner.longPredictionNanos / 1000000;

        Component message1 = Component.text()
                .append(Component.text("Milliseconds per prediction (avg. 500): ", NamedTextColor.GRAY))
                .append(Component.text(millis, NamedTextColor.WHITE))
                .build();

        Component message2 = Component.text()
                .append(Component.text("Milliseconds per prediction (avg. 20k): ", NamedTextColor.GRAY))
                .append(Component.text(longMillis, NamedTextColor.WHITE))
                .build();

        sender.sendMessage(message1);
        sender.sendMessage(message2);
    }
}
