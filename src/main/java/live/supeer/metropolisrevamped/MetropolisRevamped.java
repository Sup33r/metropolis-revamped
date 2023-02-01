package live.supeer.metropolisrevamped;

import co.aikar.commands.PaperCommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class MetropolisRevamped extends JavaPlugin {
    public Logger logger = null;
    public static MetropolisRevampedConfiguration configuration;
    public static MetropolisRevamped plugin;
    private LanguageManager languageManager;
    private static Economy econ = null;


    @Override
    public void onEnable() {
        plugin = this;
        this.logger = getLogger();
        configuration = new MetropolisRevampedConfiguration(this);
        Database.plugin = this;
        this.languageManager = new LanguageManager(this, "sv_se");

        if (!setupEconomy() ) {
            this.getLogger().severe("[Metropolis] Vault not found, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("brigadier");

        manager.registerCommand(new CommandHomeCity());
        manager.registerCommand(new CommandCity());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static MetropolisRevamped getPlugin() {
        return plugin;
    }
    public static Economy getEconomy() { return econ; }

    public void sendMessage(@NotNull CommandSender sender, @NotNull String key, String... replacements) {
        String message = this.languageManager.getValue(key, getLocale(sender), replacements);

        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    private @NotNull String getLocale(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getLocale();
        } else {
            return this.getConfig().getString("settings.locale", "sv_se");
        }
    }
}
