package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.utils.updates.GrimUpdateCheckService;

public class UpdateChecker implements StartableInitable {
    @Override
    public void start() {
        if (GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("check-for-updates", true)) {
            GrimUpdateCheckService.checkForUpdatesAsync(GrimAPI.INSTANCE.getPlatformServer().getConsoleSender());
        }
    }
}
