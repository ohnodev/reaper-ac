package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.platform.api.command.CommandService;
import ac.reaper.reaperac.utils.anticheat.LogUtil;

public record CommandRegister(CommandService service) implements StartableInitable {

    @Override
    public void start() {
        try {
            if (service != null) {
                service.registerCommands();
            }
        } catch (Throwable t) {
            // This is the ultimate safety net. If command registration fails, ReaperAC keeps running.
            LogUtil.error("Failed to register commands! ReaperAC will run without command support.", t);
        }
    }
}
