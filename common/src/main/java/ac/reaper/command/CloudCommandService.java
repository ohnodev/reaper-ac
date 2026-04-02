package ac.reaper.command;

import ac.reaper.command.commands.*;
import ac.reaper.command.handler.ReaperCommandFailureHandler;
import ac.reaper.platform.api.command.CommandService;
import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import ac.reaper.utils.anticheat.MessageUtil;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.processors.requirements.RequirementApplicable;
import org.incendo.cloud.processors.requirements.RequirementApplicable.RequirementApplicableFactory;
import org.incendo.cloud.processors.requirements.RequirementPostprocessor;
import org.incendo.cloud.processors.requirements.Requirements;

import java.util.function.Function;
import java.util.function.Supplier;

public class CloudCommandService implements CommandService {

    public static final CloudKey<Requirements<Sender, SenderRequirement>> REQUIREMENT_KEY
            = CloudKey.of("requirements", new TypeToken<>() {});

    public static final RequirementApplicableFactory<Sender, SenderRequirement> REQUIREMENT_FACTORY
            = RequirementApplicable.factory(REQUIREMENT_KEY);

    private boolean commandsRegistered = false;

    private final Supplier<CommandManager<Sender>> commandManagerSupplier;
    private final CloudCommandAdapter commandAdapter;

    public CloudCommandService(Supplier<CommandManager<Sender>> commandManagerSupplier, CloudCommandAdapter commandAdapter) {
        this.commandManagerSupplier = commandManagerSupplier;
        this.commandAdapter = commandAdapter;
    }

    public void registerCommands() {
        if (commandsRegistered) return;
        CommandManager<Sender> commandManager = commandManagerSupplier.get();
        new ReaperPerf().register(commandManager, commandAdapter);
        new ReaperDebug().register(commandManager, commandAdapter);
        new ReaperAlerts().register(commandManager, commandAdapter);
        new ReaperProfile().register(commandManager, commandAdapter);
        new ReaperSendAlert().register(commandManager, commandAdapter);
        new ReaperHelp().register(commandManager, commandAdapter);
        new ReaperHistory().register(commandManager, commandAdapter);
        new ReaperReload().register(commandManager, commandAdapter);
        new ReaperSpectate().register(commandManager, commandAdapter);
        new ReaperStopSpectating().register(commandManager, commandAdapter);
        new ReaperLog().register(commandManager, commandAdapter);
        new ReaperVerbose().register(commandManager, commandAdapter);
        new ReaperVersion().register(commandManager, commandAdapter);
        new ReaperDump().register(commandManager, commandAdapter);
        new ReaperBrands().register(commandManager, commandAdapter);
        new ReaperList().register(commandManager, commandAdapter);
        new ReaperTestWebhook().register(commandManager, commandAdapter);

        final RequirementPostprocessor<Sender, SenderRequirement>
                senderRequirementPostprocessor = RequirementPostprocessor.of(
                REQUIREMENT_KEY,
                new ReaperCommandFailureHandler()
        );
        commandManager.registerCommandPostProcessor(senderRequirementPostprocessor);
        registerExceptionHandler(commandManager, InvalidSyntaxException.class, e -> MessageUtil.miniMessage(e.correctSyntax()));
        commandsRegistered = true;
    }

    protected <E extends Exception> void registerExceptionHandler(CommandManager<Sender> commandManager, Class<E> ex, Function<E, ComponentLike> toComponent) {
        commandManager.exceptionController().registerHandler(ex,
                (c) -> c.context().sender().sendMessage(toComponent.apply(c.exception()).asComponent().colorIfAbsent(NamedTextColor.RED))
        );
    }
}
