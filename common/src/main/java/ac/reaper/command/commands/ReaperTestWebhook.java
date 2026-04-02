package ac.reaper.command.commands;

import ac.reaper.ReaperAPI;
import ac.reaper.command.BuildableCommand;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.utils.anticheat.LogUtil;
import ac.reaper.utils.anticheat.MessageUtil;
import ac.reaper.utils.data.webhook.discord.WebhookMessage;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public class ReaperTestWebhook implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudCommandAdapter adapter) {
        commandManager.command(
                commandManager.commandBuilder("reaper", "reaperac")
                        .literal("testwebhook")
                        .permission("reaper.testwebhook")
                        .handler(this::handleTestWebhook)
        );
    }

    private void handleTestWebhook(@NotNull CommandContext<Sender> context) {
        if (ReaperAPI.INSTANCE.getDiscordManager().isDisabled()) {
            context.sender().sendMessage(MessageUtil.miniMessage(ReaperAPI.INSTANCE.getConfigManager().getWebhookNotEnabled()));
            return;
        }

        WebhookMessage webhookMessage = new WebhookMessage().content(ReaperAPI.INSTANCE.getConfigManager().getWebhookTestMessage());
        ReaperAPI.INSTANCE.getDiscordManager().sendWebhookMessage(webhookMessage).whenCompleteAsync(((successful, throwable) -> {
            if (successful == true) {
                context.sender().sendMessage(MessageUtil.miniMessage(ReaperAPI.INSTANCE.getConfigManager().getWebhookTestSucceeded()));
                return;
            }

            context.sender().sendMessage(MessageUtil.miniMessage(ReaperAPI.INSTANCE.getConfigManager().getWebhookTestFailed()));

            if (throwable != null) {
                LogUtil.error("Exception caught while sending a Discord webhook test alert", throwable);
            }
        }));
    }
}
