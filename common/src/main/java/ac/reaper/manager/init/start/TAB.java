package ac.reaper.manager.init.start;

import ac.reaper.ReaperAPI;

public class TAB implements StartableInitable {

    @Override
    public void start() {
        if (ReaperAPI.INSTANCE.getPluginManager().getPlugin("TAB") == null) {
            return;
        }
        // Reaper AC runs native protocol only; Via/TAB compatibility warning is not relevant.
    }
}
