package ac.reaper.platform.fabric.initables;

import ac.reaper.ReaperAPI;
import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.manager.init.stop.StoppableInitable;
import ac.reaper.platform.fabric.utils.metrics.MetricsFabric;
import ac.reaper.utils.anticheat.Constants;

public class FabricBStats implements StartableInitable, StoppableInitable {

    private MetricsFabric metricsFabric;

    @Override
    public void start() {
        try {
            metricsFabric = new MetricsFabric(ReaperAPI.INSTANCE.getReaperPlugin(), Constants.BSTATS_PLUGIN_ID);
        } catch (Exception ignored) {}
    }

    @Override
    public void stop() {
        if (metricsFabric != null)
            metricsFabric.shutdown();
    }
}
