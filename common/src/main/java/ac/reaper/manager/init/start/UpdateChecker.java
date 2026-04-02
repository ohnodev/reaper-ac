package ac.reaper.manager.init.start;

import ac.reaper.ReaperAPI;
import ac.reaper.command.commands.ReaperVersion;

public class UpdateChecker implements StartableInitable {
    @Override
    public void start() {
        if (ReaperAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("check-for-updates", true)) {
            ReaperVersion.checkForUpdatesAsync(ReaperAPI.INSTANCE.getPlatformServer().getConsoleSender());
        }
    }
}
