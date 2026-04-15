package ac.reaper.reaperac.utils.legacylink;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.data.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional console logging for Grim transaction pings/pongs on LegacyLink (26.1) clients.
 * Enable with {@value #CONFIG_KEY} in ReaperAC {@code config.yml}.
 */
public final class LegacyLinkGrimTransactionDebug {

    public static final String CONFIG_KEY = "debug-legacylink-transaction-pings";

    private static final ConcurrentHashMap<String, Long> LAST_PING_LOG_MS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> LAST_PONG_OK_LOG_MS = new ConcurrentHashMap<>();

    private LegacyLinkGrimTransactionDebug() {
    }

    private static boolean transactionIdPresentInSentQueue(GrimPlayer grim, short id) {
        for (Pair<Short, Long> p : grim.transactionsSent) {
            if (p.first() == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEnabled() {
        return GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse(CONFIG_KEY, false);
    }

    /**
     * Legacy vanilla ping path: log periodically; always warn on bad state.
     */
    public static void logLegacyVanillaPingEnqueued(String playerName, short id, @Nullable GrimPlayer grim, boolean removedPending) {
        if (grim == null) {
            LogUtil.warn("[GrimTxDebug][legacy-ping] player=" + playerName + " id=" + id + " grimUser=MISSING (cannot ack transaction)");
            return;
        }
        if (!removedPending) {
            // Vanilla ClientboundPingPacket often passes through PE's encoder after connection.send(); PacketPingListener
            // then moves the id from didWeSendThatTrans into transactionsSent before acknowledgeVanillaPingDispatched runs.
            if (isEnabled() && transactionIdPresentInSentQueue(grim, id)) {
                long now = System.currentTimeMillis();
                Long prev = LAST_PING_LOG_MS.put(playerName, now);
                if (prev != null && now - prev < 2000L) {
                    return;
                }
                LogUtil.info("[GrimTxDebug][legacy-ping] player=" + playerName + " id=" + id
                        + " pendingRemove=noop (PE outbound already recorded ping — expected, not a bug)");
                return;
            }
            LogUtil.warn("[GrimTxDebug][legacy-ping] player=" + playerName + " id=" + id
                    + " pendingRemove=FAIL (id missing from didWeSendThatTrans and not in sent queue — real ordering bug)");
            return;
        }
        if (!isEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long prev = LAST_PING_LOG_MS.put(playerName, now);
        if (prev != null && now - prev < 2000L) {
            return;
        }
        LogUtil.info("[GrimTxDebug][legacy-ping] player=" + playerName + " id=" + id + " vanillaPingEnqueued+acked");
    }

    /**
     * PE-decoded pong for Grim transaction ids ({@code <= 0}).
     */
    public static void logPong(String playerName, short id, boolean matched) {
        if (!matched) {
            if (isEnabled()) {
                LogUtil.warn("[GrimTxDebug][pong] player=" + playerName + " id=" + id
                        + " matchedTransaction=false (no head of queue / wrong id — expect stall then disconnect.timeout)");
            }
            return;
        }
        if (!isEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long prev = LAST_PONG_OK_LOG_MS.put(playerName, now);
        if (prev != null && now - prev < 2000L) {
            return;
        }
        LogUtil.info("[GrimTxDebug][pong] player=" + playerName + " id=" + id + " matchedTransaction=true");
    }
}
