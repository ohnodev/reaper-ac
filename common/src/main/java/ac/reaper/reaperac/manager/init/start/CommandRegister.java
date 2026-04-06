package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.platform.api.command.CommandService;

public record CommandRegister(CommandService service) implements StartableInitable {

    @Override
    public void start() {
        // Commands are intentionally disabled in this fork.
    }
}
