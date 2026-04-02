package ac.reaper.manager.violationdatabase;

import ac.reaper.player.ReaperPlayer;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface ViolationDatabase {

    void connect() throws SQLException;

    void logAlert(ReaperPlayer player, String reaperVersion, String verbose, String checkName, int vls);

    int getLogCount(UUID player);

    List<Violation> getViolations(UUID player, int page, int limit);

    void disconnect();

}
