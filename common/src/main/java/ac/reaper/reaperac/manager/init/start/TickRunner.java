package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.platform.api.Platform;
import ac.reaper.reaperac.utils.anticheat.LogUtil;

public class TickRunner implements StartableInitable {
    @Override
    public void start() {
        LogUtil.info("Registering tick schedulers...");

        if (GrimAPI.INSTANCE.getPlatform() == Platform.FOLIA) {
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(GrimAPI.INSTANCE.getReaperPlugin(), () -> {
                GrimAPI.INSTANCE.getTickManager().tickSync();
                GrimAPI.INSTANCE.getTickManager().tickAsync();
            }, 1, 1);
        } else {
            GrimAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().runAtFixedRate(GrimAPI.INSTANCE.getReaperPlugin(), () -> GrimAPI.INSTANCE.getTickManager().tickSync(), 0, 1);
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(GrimAPI.INSTANCE.getReaperPlugin(), () -> GrimAPI.INSTANCE.getTickManager().tickAsync(), 0, 1);
        }
    }
}
