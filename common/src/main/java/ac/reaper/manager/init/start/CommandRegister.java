package ac.reaper.manager.init.start;

import ac.reaper.platform.api.command.CommandService;
import ac.reaper.utils.anticheat.LogUtil;

public record CommandRegister(CommandService service) implements StartableInitable {

    @Override
    public void start() {
        try {
            if (service != null) {
                service.registerCommands();
            }
        } catch (Throwable t) {
            // This is the ultimate safety net. If command registration fails, Reaper keeps running.
            LogUtil.error("Failed to register commands! Reaper will run without command support.", t);
        }
    }
}
