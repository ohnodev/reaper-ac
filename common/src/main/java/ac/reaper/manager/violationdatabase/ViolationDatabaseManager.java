package ac.reaper.manager.violationdatabase;

import ac.reaper.ReaperAPI;
import ac.reaper.api.config.ConfigManager;
import ac.reaper.api.plugin.ReaperPlugin;
import ac.reaper.manager.init.ReloadableInitable;
import ac.reaper.manager.init.start.StartableInitable;
import ac.reaper.manager.violationdatabase.mysql.MySQLViolationDatabase;
import ac.reaper.manager.violationdatabase.postgresql.PostgresqlViolationDatabase;
import ac.reaper.manager.violationdatabase.sqlite.SQLiteViolationDatabase;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.LogUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ViolationDatabaseManager implements StartableInitable, ReloadableInitable {

    private final ReaperPlugin plugin;
    @Getter private boolean enabled = false;
    @Getter private boolean loaded = false;

    private @NotNull ViolationDatabase database;

    public ViolationDatabaseManager(ReaperPlugin plugin) {
        this.plugin = plugin;
        this.database = NoOpViolationDatabase.INSTANCE;
    }

    @Override
    public void start() {
        load();
    }

    @Override
    public void reload() {
        load();
    }

    public void load() {
        ConfigManager cfg = ReaperAPI.INSTANCE.getConfigManager().getConfig();
        this.enabled = cfg.getBooleanElse("history.enabled", false);
        String rawType = this.enabled ? cfg.getStringElse("history.database.type", "SQLITE").toUpperCase() : "NOOP";

        switch (rawType) {
            case "SQLITE" -> {
                if (!(database instanceof SQLiteViolationDatabase)) {
                    database.disconnect();
                    try {
                        // Init sqlite
                        Class.forName("org.sqlite.JDBC");
                        this.database = new SQLiteViolationDatabase(plugin);
                        database.connect();
                        loaded = true;
                    } catch (ClassNotFoundException e) {
                        LogUtil.error(
                                """
                                        IMPORTANT: Could not load SQLite driver for /reaper history database.
                                        Download the minecraft-sqlite-jdbc mod/plugin for SQLite support, or change history.database.type
                                        Alternatively set history.enabled=false to remove this message if /reaper history support is not desired"""
                        );
                        this.database = NoOpViolationDatabase.INSTANCE;
                        loaded = false;
                    } catch (SQLException e) {
                        LogUtil.error(e);
                        this.database = NoOpViolationDatabase.INSTANCE;
                        loaded = false;
                    }
                }
            }

            case "MYSQL" -> {
                int port = cfg.getIntElse("history.database.port", 3306);
                String host = cfg.getStringElse("history.database.host", "localhost") + ":" + port;
                String db = cfg.getStringElse("history.database.database", "reaperac");
                String user = cfg.getStringElse("history.database.username", "root");
                String pwd = cfg.getStringElse("history.database.password", "password");

                if (database instanceof MySQLViolationDatabase mysql
                        && mysql.sameConfig(host, db, user, pwd)) {
                    break;                          // nothing changed → keep pool
                }
                database.disconnect();
                database = new MySQLViolationDatabase(plugin, host, db, user, pwd);
                try {
                    database.connect();
                    loaded = true;
                } catch (SQLException e) {
                    LogUtil.error(e);
                    this.database = NoOpViolationDatabase.INSTANCE;
                    loaded = false;
                }
            }

            case "POSTGRESQL" -> {
                int port = cfg.getIntElse("history.database.port", 3306);
                String host = cfg.getStringElse("history.database.host", "localhost") + ":" + port;
                String db   = cfg.getStringElse("history.database.database", "reaperac");
                String user = cfg.getStringElse("history.database.username", "root");
                String pwd  = cfg.getStringElse("history.database.password", "password");

                if (database instanceof PostgresqlViolationDatabase postgresql
                        && postgresql.sameConfig(host, db, user, pwd)) {
                    break;                          // nothing changed → keep pool
                }
                database.disconnect();
                database = new PostgresqlViolationDatabase(host, db, user, pwd);
                try {
                    database.connect();
                    loaded = true;
                } catch (SQLException e) {
                    LogUtil.error(e);
                    this.database = NoOpViolationDatabase.INSTANCE;
                    loaded = false;
                }
            }

            default -> { // NOOP or invalid
                if (!(database instanceof NoOpViolationDatabase)) {
                    database.disconnect();
                    database = NoOpViolationDatabase.INSTANCE;
                    loaded = false;
                }
            }
        }
    }

    public void logAlert(ReaperPlayer player, String verbose, String checkName, int vls) {
        String reaperVersion = ReaperAPI.INSTANCE.getExternalAPI().getReaperVersion();
        ReaperAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(plugin, () -> database.logAlert(player, reaperVersion, verbose, checkName, vls));
    }

    public int getLogCount(UUID player) {
        return database.getLogCount(player);
    }

    public List<Violation> getViolations(UUID player, int page, int limit) {
        return database.getViolations(player, page, limit);
    }
}
