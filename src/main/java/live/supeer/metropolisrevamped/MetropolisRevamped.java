package live.supeer.metropolisrevamped;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MetropolisRevamped extends JavaPlugin {
    public Logger logger = null;
    public static MetropolisRevampedConfiguration configuration;
    public static MetropolisRevamped plugin;


    @Override
    public void onEnable() {
        plugin = this;
        this.logger = getLogger();
        configuration = new MetropolisRevampedConfiguration(this);
        Database.plugin = this;

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MetropolisRevamped getPlugin() {
        return plugin;
    }
}
