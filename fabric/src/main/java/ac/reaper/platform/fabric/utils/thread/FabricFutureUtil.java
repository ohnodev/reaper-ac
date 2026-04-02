package ac.reaper.platform.fabric.utils.thread;

import ac.reaper.ReaperAPI;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FabricFutureUtil {
    public static <U> CompletableFuture<U> supplySync(Supplier<U> entityTeleportSupplier) {
        CompletableFuture<U> ret = new CompletableFuture<>();
        ReaperAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().run(ReaperAPI.INSTANCE.getReaperPlugin(),
                () -> ret.complete(entityTeleportSupplier.get()));
        return ret;
    }
}
