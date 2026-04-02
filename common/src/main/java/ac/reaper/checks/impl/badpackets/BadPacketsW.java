package ac.reaper.checks.impl.badpackets;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.player.ReaperPlayer;

@CheckData(name = "BadPacketsW", description = "Interacted with non-existent entity", experimental = true)
public class BadPacketsW extends Check {
    public BadPacketsW(ReaperPlayer player) {
        super(player);
    }
}
