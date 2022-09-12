package cat.nyaa.namerecorder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerJoinEventHandler implements Listener {

    private final long delay;

    private final Plugin plugin;

    private final AsyncTaskManager mgr;

    private final PlayerNameDatabase db;

    private List<PlayerNameRecord> cache;

    public PlayerJoinEventHandler(long delay, Plugin plugin, AsyncTaskManager mgr, PlayerNameDatabase db) {
        this.delay = delay;
        this.plugin = plugin;
        this.mgr = mgr;
        this.db = db;
        this.cache = new ArrayList<>();
    }

    public List<PlayerNameRecord> swap() {
        List<PlayerNameRecord> data = this.cache;
        this.cache = new ArrayList<>();
        return data;
    }

    private void addAsyncTask(Runnable task) {
        if(!this.mgr.add(task)) {
            this.plugin.getLogger().log(Level.WARNING, "fail to create task");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        Instant now = Instant.now();
        PlayerNameRecord r = new PlayerNameRecord(p.getUniqueId(), p.getName(), now, 1);
        if(this.delay > 0) {
            if(this.cache.isEmpty()) {
                this.plugin.getServer()
                        .getScheduler()
                        .runTaskLater(
                                this.plugin,
                                () -> this.addAsyncTask(new MultiUpdateTask(this.swap(), this.db, this.plugin.getLogger())),
                                this.delay
                            );
            }
            this.cache.add(r);
        } else {
            this.addAsyncTask(new SingleUpdateTask(r, this.db, this.plugin.getLogger()));
        }
    }
}

class SingleUpdateTask implements Runnable {

    private final PlayerNameRecord record;
    private final PlayerNameDatabase db;
    private final Logger logger;

    SingleUpdateTask(PlayerNameRecord record, PlayerNameDatabase db, Logger logger) {
        this.record = record;
        this.db = db;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            this.db.update(record, this.logger);
        } catch (SQLException e) {
            this.logger.log(Level.WARNING, "sql error", e);
        }
    }
}


class MultiUpdateTask implements Runnable {

    private final List<PlayerNameRecord> records;
    private final PlayerNameDatabase db;
    private final Logger logger;

    MultiUpdateTask(List<PlayerNameRecord> records, PlayerNameDatabase db, Logger logger) {
        this.records = records;
        this.db = db;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            this.db.updateBatch(this.records, this.logger);
        } catch (SQLException e) {
            this.logger.log(Level.WARNING, "sql error", e);
        } catch (PlayerNameDatabase.MultiSQLException e) {
            for(SQLException e1 : e.exceptions) {
                this.logger.log(Level.WARNING, "sql batch error", e1);
            }
        }
    }
}