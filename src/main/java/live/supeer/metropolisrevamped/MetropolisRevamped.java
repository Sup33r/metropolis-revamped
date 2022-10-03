package live.supeer.metropolisrevamped;

import co.aikar.commands.PaperCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public final class MetropolisRevamped extends JavaPlugin {
    public Logger logger = null;
    public static MetropolisRevampedConfiguration configuration;
    public static MetropolisRevamped plugin;
    private LanguageManager languageManager;


    @Override
    public void onEnable() {
        plugin = this;
        this.logger = getLogger();
        configuration = new MetropolisRevampedConfiguration(this);
        Database.plugin = this;
        this.languageManager = new LanguageManager(this, "sv_se");

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("brigadier");

        manager.registerCommand(new CommandHomeCity());
        manager.registerCommand(new CommandCity());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MetropolisRevamped getPlugin() {
        return plugin;
    }

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
