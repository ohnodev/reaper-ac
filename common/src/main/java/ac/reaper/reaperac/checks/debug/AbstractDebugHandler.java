package ac.reaper.reaperac.checks.debug;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.player.GrimPlayer;

public abstract class AbstractDebugHandler extends Check {
    public AbstractDebugHandler(GrimPlayer player) {
        super(player);
    }

    public abstract void toggleListener(GrimPlayer player);

    public abstract boolean toggleConsoleOutput();
}
