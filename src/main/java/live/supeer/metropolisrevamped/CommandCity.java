package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.Arrays;

@CommandAlias("city|c")
public class CommandCity extends BaseCommand {
    static MetropolisRevamped plugin;
    @Subcommand("info")
    @Default
    public static void onInfo(Player player, @Optional String cityName) {

    }

    @Subcommand("bank")
    @Syntax("§7Syntax: /city bank\n§7Syntax: /city bank +§nbelopp§r§7 §nstad\n§7Syntax: /city bank -§nbelopp§r§7 §nanledning")
    public static void onBank(Player player, @Optional String[] args) {
        if (!player.hasPermission("metropolis.city.bank")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        Economy economy = MetropolisRevamped.getEconomy();

        if (args.length == 0) {
            if (HCDatabase.getHomeCity(player.getUniqueId().toString()) == null) {
                plugin.sendMessage(player,"messages.error.missing.homeCity");
                return;
            }
            String homeCity = HCDatabase.getHomeCity(player.getUniqueId().toString());
            String cityBalance = Utilities.formattedMoney(CityDatabase.getCityBalance(homeCity));
            plugin.sendMessage(player,"messages.city.balance","%balance%",cityBalance,"%cityname%",homeCity);
            return;
        }
        if (args[0].startsWith("+")) {
            int inputBalance = Integer.parseInt(args[0].replaceAll("[^0-9]",""));
            String cityName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String playerCity = HCDatabase.getHomeCity(player.getUniqueId().toString());

            if (!CityDatabase.cityExists(cityName)) {
                plugin.sendMessage(player,"messages.error.missing.city");
                return;
            }

            if (economy.getBalance(player) < inputBalance) {
                plugin.sendMessage(player,"messages.error.missing.playerBalance","%cityname%",playerCity);
                return;
            }

            economy.withdrawPlayer(player,inputBalance);
            CityDatabase.addCityBalance(cityName,inputBalance);
            plugin.sendMessage(player,"messages.city.successful.deposit","%amount%",Utilities.formattedMoney(inputBalance),"%cityname%",cityName);
        }
        if (args[0].startsWith("-")) {
            if (HCDatabase.getHomeCity(player.getUniqueId().toString()) == null) {
                plugin.sendMessage(player,"messages.error.missing.homeCity");
                return;
            }
            int inputBalance = Integer.parseInt(args[0].replaceAll("[^0-9]",""));
            String inputBalanceFormatted = Utilities.formattedMoney(inputBalance);
            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            String playerCity = HCDatabase.getHomeCity(player.getUniqueId().toString());
            int cityBalance = CityDatabase.getCityBalance(playerCity);
            String cityRole = CityDatabase.getCityRole(playerCity,player.getUniqueId().toString());

            if (cityRole == null || cityRole.equals("member") || cityRole.equals("inviter") || cityRole.equals("assistant")) {
                plugin.sendMessage(player,"messages.error.permissionDenied");
                return;
            }


            if (!(reason.length() >= 8)) {
                plugin.sendMessage(player,"messages.error.missing.reasonLength","%cityname%",playerCity);
                return;
            }

            if (cityBalance <= 100000 || inputBalance > cityBalance - 100000) {
                plugin.sendMessage(player,"messages.error.missing.balance","%cityname%",playerCity);
                return;
            }

            CityDatabase.removeCityBalance(playerCity,inputBalance);
            economy.depositPlayer(player,inputBalance);

            plugin.sendMessage(player,"messages.city.successful.withdraw","%amount%",inputBalanceFormatted,"%cityname%",playerCity);
        }
    }
}
