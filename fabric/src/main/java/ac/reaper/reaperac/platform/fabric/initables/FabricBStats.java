package ac.reaper.reaperac.platform.fabric.initables;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.manager.init.start.StartableInitable;
import ac.reaper.reaperac.manager.init.stop.StoppableInitable;
import ac.reaper.reaperac.platform.fabric.utils.metrics.MetricsFabric;
import ac.reaper.reaperac.utils.anticheat.Constants;

public class FabricBStats implements StartableInitable, StoppableInitable {

    private MetricsFabric metricsFabric;

    @Override
    public void start() {
        try {
            metricsFabric = new MetricsFabric(GrimAPI.INSTANCE.getGrimPlugin(), Constants.BSTATS_PLUGIN_ID);
        } catch (Exception ignored) {}
    }

    @Override
    public void stop() {
        if (metricsFabric != null)
            metricsFabric.shutdown();
    }
}
