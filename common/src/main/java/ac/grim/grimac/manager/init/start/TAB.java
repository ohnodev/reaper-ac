package ac.grim.grimac.manager.init.start;

import ac.grim.grimac.GrimAPI;

public class TAB implements StartableInitable {

    @Override
    public void start() {
        if (GrimAPI.INSTANCE.getPluginManager().getPlugin("TAB") == null) {
            return;
        }
        // Reaper AC runs native protocol only; Via/TAB compatibility warning is not relevant.
    }
}
