package ac.reaper.checks.impl.badpackets;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.player.ReaperPlayer;

@CheckData(name = "BadPacketsN", setback = 0)
public class BadPacketsN extends Check {
    public BadPacketsN(final ReaperPlayer player) {
        super(player);
    }
}
