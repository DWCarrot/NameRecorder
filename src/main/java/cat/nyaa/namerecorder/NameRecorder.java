package cat.nyaa.namerecorder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class NameRecorder extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ImmutableConfig cfg = new ImmutableConfig(this);


        File dbFile = new File(cfg.dbFile);
        if(!dbFile.isAbsolute()) {
            dbFile = new File(this.getDataFolder(), cfg.dbFile);
        }
        try {
            AsyncTaskManager mgr = new AsyncTaskManager(this, 256);
            Class.forName("org.sqlite.JDBC");
            PlayerNameDatabase db = new PlayerNameDatabase("jdbc:sqlite:" + dbFile.getPath());
            this.getServer().getPluginManager().registerEvents(new PlayerJoinEventHandler(cfg.delay * 20L, this, mgr, db), this);
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "fail to start.", e);
        }
        this.getLogger().log(Level.INFO, "loaded success");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
