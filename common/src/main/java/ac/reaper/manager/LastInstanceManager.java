package ac.reaper.manager;

import ac.reaper.checks.Check;
import ac.reaper.checks.type.PostPredictionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import ac.reaper.utils.data.LastInstance;

import java.util.ArrayList;
import java.util.List;

public class LastInstanceManager extends Check implements PostPredictionCheck {
    private final List<LastInstance> instances = new ArrayList<>();

    public LastInstanceManager(ReaperPlayer player) {
        super(player);
    }

    public void addInstance(LastInstance instance) {
        instances.add(instance);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        for (LastInstance instance : instances) {
            instance.tick();
        }
    }
}
