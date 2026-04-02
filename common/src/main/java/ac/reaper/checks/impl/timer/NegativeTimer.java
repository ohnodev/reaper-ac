package ac.reaper.checks.impl.timer;

import ac.reaper.api.config.ConfigManager;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.PostPredictionCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "NegativeTimer", setback = -1, experimental = true)
public class NegativeTimer extends Timer implements PostPredictionCheck {

    public NegativeTimer(ReaperPlayer player) {
        super(player);
        timerBalanceRealTime = System.nanoTime() + clockDrift;
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        // We can't negative timer check a 1.9+ player who is standing still.
        if (player.uncertaintyHandler.lastPointThree.hasOccurredSince(2) || !predictionComplete.isChecked()) {
            timerBalanceRealTime = System.nanoTime() + clockDrift;
        }

        if (timerBalanceRealTime < lastMovementPlayerClock - clockDrift) {
            int lostMS = (int) ((System.nanoTime() - timerBalanceRealTime) / 1e6);
            flagAndAlertWithSetback("-" + lostMS);
            timerBalanceRealTime += 50e6;
        }
    }

    @Override
    public void doCheck(final PacketReceiveEvent event) {
        // We don't know if the player is ticking stable, therefore we must wait until prediction
        // determines this.  Do nothing here!
    }

    @Override
    public void onReload(ConfigManager config) {
        clockDrift = (long) (config.getDoubleElse(getConfigName() + ".drift", 1200.0) * 1e6);
    }
}
