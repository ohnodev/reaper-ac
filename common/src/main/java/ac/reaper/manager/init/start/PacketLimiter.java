package ac.reaper.manager.init.start;

import ac.reaper.ReaperAPI;
import ac.reaper.player.ReaperPlayer;

public class PacketLimiter implements StartableInitable {
    @Override
    public void start() {
        ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runAtFixedRate(ReaperAPI.INSTANCE.getReaperPlugin(), () -> {
            for (ReaperPlayer player : ReaperAPI.INSTANCE.getPlayerDataManager().getEntries()) {
                // Avoid concurrent reading on an integer as it's results are unknown
                player.cancelledPackets.set(0);
            }
        }, 1, 20);
    }
}
