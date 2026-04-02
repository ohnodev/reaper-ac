package ac.reaper.command.handler;

import ac.reaper.command.SenderRequirement;
import ac.reaper.platform.api.sender.Sender;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.processors.requirements.RequirementFailureHandler;
import org.jetbrains.annotations.NotNull;

public class ReaperCommandFailureHandler implements RequirementFailureHandler<Sender, SenderRequirement> {
    @Override
    public void handleFailure(@NotNull CommandContext<Sender> context, @NotNull SenderRequirement requirement) {
        context.sender().sendMessage(requirement.errorMessage(context.sender()));
    }
}
