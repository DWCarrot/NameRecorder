package cat.nyaa.namerecorder;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ImmutableConfig {

    public final String dbFile;

    public final long delay;

    public ImmutableConfig(Plugin plugin) {
        boolean changed = false;
        FileConfiguration cfg = plugin.getConfig();

        String key1 = "db_file";
        String value1 = cfg.getString(key1, null);
        if(value1 == null) {
            value1 = "PlayerName.db";
            cfg.set(key1, value1);
            changed = true;
        }
        this.dbFile = value1;

        String key2 = "delay";
        long value2 = cfg.getLong(key2, -1);
        if(value2 < 0) {
            value2 = 5;
            cfg.set(key2, value2);
            changed = true;
        }
        this.delay = value2;

        if(changed) {
            try {
                plugin.getDataFolder().mkdirs();
                cfg.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "unable to save config", e);
            }
        }
    }
}
