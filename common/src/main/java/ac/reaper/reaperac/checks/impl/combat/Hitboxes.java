package ac.reaper.reaperac.checks.impl.combat;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.player.GrimPlayer;

@CheckData(name = "Hitboxes", setback = 10)
public class Hitboxes extends Check {
    public Hitboxes(GrimPlayer player) {
        super(player);
    }
}
