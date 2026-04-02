package ac.reaper.checks.impl.combat;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.player.ReaperPlayer;

@CheckData(name = "Hitboxes", setback = 10)
public class Hitboxes extends Check {
    public Hitboxes(ReaperPlayer player) {
        super(player);
    }
}
