package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
@CommandAlias("c")
public class CommandCity extends BaseCommand {
    static MetropolisRevamped plugin;
    static CityDatabase cityDatabase;
    static HCDatabase homeCityDatabase;
    @Subcommand("info")
    @Default
    public static void onInfo(Player player, @Optional String cityName) {

    }

    @Subcommand("bank")
    @Syntax("§7Syntax: /city bank\nTest")
    public static void onBank(Player player, @Optional String input, @Optional String reason) {
        if (!player.hasPermission("metropolis.city.bank")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        Economy economy = MetropolisRevamped.getEconomy();
        if (input.isEmpty()) {
            if (homeCityDatabase.getHomeCity(player.getUniqueId().toString()) == null) {
                plugin.sendMessage(player,"messages.error.noHomeCity");
                return;
            }
            String homeCity = homeCityDatabase.getHomeCity(player.getUniqueId().toString());
            String cityBalance = Utilities.formattedMoney(cityDatabase.getCityBalance(homeCity));
            plugin.sendMessage(player,"messages.city.balance","%balance%",cityBalance,"%cityname%",homeCity);
        }
        if (input.startsWith("+")) {
            int balance = Integer.parseInt(input.replaceAll("[0-9]",""));
        }
        if (input.startsWith("-")) {

        }
    }
}
