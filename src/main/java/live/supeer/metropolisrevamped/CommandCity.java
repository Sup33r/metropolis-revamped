package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
@CommandAlias("c")
public class CommandCity extends BaseCommand {

    @Subcommand("info")
    @Default
    public static void onInfo(Player player, @Optional String cityName) {

    }

    @Subcommand("bank")
    public static void onBank(Player player, @Optional String balance) {
        Economy economy = MetropolisRevamped.getEconomy();

    }
}
