package ac.reaper.checks.impl.misc;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.player.ReaperPlayer;

@CheckData(name = "TransactionOrder")
public class TransactionOrder extends Check {
    public TransactionOrder(ReaperPlayer player) {
        super(player);
    }
}
