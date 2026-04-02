package ac.reaper.checks.debug;

import ac.reaper.checks.Check;
import ac.reaper.player.ReaperPlayer;

public abstract class AbstractDebugHandler extends Check {
    public AbstractDebugHandler(ReaperPlayer player) {
        super(player);
    }

    public abstract void toggleListener(ReaperPlayer player);

    public abstract boolean toggleConsoleOutput();
}
