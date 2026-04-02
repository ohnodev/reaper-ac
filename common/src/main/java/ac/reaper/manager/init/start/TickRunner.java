package ac.reaper.manager.init.start;

import ac.reaper.ReaperAPI;
import ac.reaper.platform.api.Platform;
import ac.reaper.utils.anticheat.LogUtil;

public class TickRunner implements StartableInitable {
    @Override
    public void start() {
        LogUtil.info("Registering tick schedulers...");

        if (ReaperAPI.INSTANCE.getPlatform() == Platform.FOLIA) {
            ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(ReaperAPI.INSTANCE.getReaperPlugin(), () -> {
                ReaperAPI.INSTANCE.getTickManager().tickSync();
                ReaperAPI.INSTANCE.getTickManager().tickAsync();
            }, 1, 1);
        } else {
            ReaperAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().runAtFixedRate(ReaperAPI.INSTANCE.getReaperPlugin(), () -> ReaperAPI.INSTANCE.getTickManager().tickSync(), 0, 1);
            ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(ReaperAPI.INSTANCE.getReaperPlugin(), () -> ReaperAPI.INSTANCE.getTickManager().tickAsync(), 0, 1);
        }
    }
}
