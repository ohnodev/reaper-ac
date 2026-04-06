package ac.reaper.reaperac.checks.impl.misc;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.player.GrimPlayer;

@CheckData(name = "TransactionOrder")
public class TransactionOrder extends Check {
    public TransactionOrder(GrimPlayer player) {
        super(player);
    }
}
