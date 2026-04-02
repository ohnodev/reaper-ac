package ac.reaper.command.requirements;

import ac.reaper.command.SenderRequirement;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.utils.anticheat.MessageUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public final class PlayerSenderRequirement implements SenderRequirement {

    public static final PlayerSenderRequirement PLAYER_SENDER_REQUIREMENT = new PlayerSenderRequirement();

    @Override
    public @NotNull Component errorMessage(Sender sender) {
        return MessageUtil.getParsedComponent(sender, "run-as-player", "%prefix% &cThis command can only be used by players!");
    }

    @Override
    public boolean evaluateRequirement(@NotNull CommandContext<Sender> commandContext) {
        return commandContext.sender().isPlayer();
    }
}
