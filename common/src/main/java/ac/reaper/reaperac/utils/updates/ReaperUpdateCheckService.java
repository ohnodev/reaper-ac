package ac.reaper.reaperac.utils.updates;

import ac.reaper.reaperac.platform.api.sender.Sender;

public final class ReaperUpdateCheckService {
    private ReaperUpdateCheckService() {
    }

    public static void checkForUpdatesAsync(Sender sender) {
        GrimUpdateCheckService.checkForUpdatesAsync(sender);
    }
}
