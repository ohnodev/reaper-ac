package ac.reaper.command;

import ac.reaper.platform.api.manager.cloud.CloudCommandAdapter;
import ac.reaper.platform.api.sender.Sender;
import org.incendo.cloud.CommandManager;

public interface BuildableCommand {
    void register(CommandManager<Sender> manager, CloudCommandAdapter adapter);
}
