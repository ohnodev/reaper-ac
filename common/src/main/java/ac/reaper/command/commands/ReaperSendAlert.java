package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.utils.anticheat.MessageUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class ReaperSendAlert implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("sendalert")
                        .permission("reaper.sendalert")
                        .required("message", StringParser.greedyStringParser())
                        .handler(this::handleSendAlert)
        );
    }

    private void handleSendAlert(@NotNull CommandContext<Sender> context) {
        String string = context.get("message");
        string = MessageUtil.replacePlaceholders((Sender) null, string);
        Component message = MessageUtil.miniMessage(string);
        ReaperAPI.INSTANCE.getAlertManager().sendAlert(message, null);
    }
}
