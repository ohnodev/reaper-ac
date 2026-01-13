package ac.grim.grimac.command;

import ac.grim.grimac.command.commands.*;
import ac.grim.grimac.command.handler.GrimCommandFailureHandler;
import ac.grim.grimac.platform.api.command.CommandService;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.utils.anticheat.MessageUtil;
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

public class CloudCommandService implements CommandService {

    public static final CloudKey<Requirements<Sender, SenderRequirement>> REQUIREMENT_KEY
            = CloudKey.of("requirements", new TypeToken<>() {});

    public static final RequirementApplicableFactory<Sender, SenderRequirement> REQUIREMENT_FACTORY
            = RequirementApplicable.factory(REQUIREMENT_KEY);

    private boolean commandsRegistered = false;

    private final CommandManager<Sender> commandManager;

    public CloudCommandService(CommandManager<Sender> commandManager) {
        this.commandManager = commandManager;
    }

    // Public static method that can be called on platforms where command must be registered earlier than InitManager.load()
    public void registerCommands() {
        if (commandsRegistered) return;
        new GrimPerf().register(commandManager);
        new GrimDebug().register(commandManager);
        new GrimAlerts().register(commandManager);
        new GrimProfile().register(commandManager);
        new GrimSendAlert().register(commandManager);
        new GrimHelp().register(commandManager);
        new GrimHistory().register(commandManager);
        new GrimReload().register(commandManager);
        new GrimSpectate().register(commandManager);
        new GrimStopSpectating().register(commandManager);
        new GrimLog().register(commandManager);
        new GrimVerbose().register(commandManager);
        new GrimVersion().register(commandManager);
        new GrimDump().register(commandManager);
        new GrimBrands().register(commandManager);
        new GrimList().register(commandManager);

        final RequirementPostprocessor<Sender, SenderRequirement>
                senderRequirementPostprocessor = RequirementPostprocessor.of(
                REQUIREMENT_KEY,
                new GrimCommandFailureHandler()
        );
        commandManager.registerCommandPostProcessor(senderRequirementPostprocessor);
        registerExceptionHandler(InvalidSyntaxException.class, e -> MessageUtil.miniMessage(e.correctSyntax()));
        commandsRegistered = true;
    }

    protected <E extends Exception> void registerExceptionHandler(Class<E> ex, Function<E, ComponentLike> toComponent) {
        commandManager.exceptionController().registerHandler(ex,
                (c) -> c.context().sender().sendMessage(toComponent.apply(c.exception()).asComponent().colorIfAbsent(NamedTextColor.RED))
        );
    }
}
