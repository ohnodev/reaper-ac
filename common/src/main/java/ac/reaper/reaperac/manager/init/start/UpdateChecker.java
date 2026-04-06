package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.ReaperAPI;
import ac.reaper.reaperac.utils.updates.ReaperUpdateCheckService;

public class UpdateChecker implements StartableInitable {
    @Override
    public void start() {
        if (ReaperAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("check-for-updates", true)) {
            ReaperUpdateCheckService.checkForUpdatesAsync(ReaperAPI.INSTANCE.getPlatformServer().getConsoleSender());
        }
    }
}
