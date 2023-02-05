package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

@CommandAlias("city|c")
public class CommandCity extends BaseCommand {
    static MetropolisRevamped plugin;
    @Subcommand("info")
    @Default
    public static void onInfo(Player player, @Optional String cityName) {

    }

    @Subcommand("bank")
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
            if (args[0].substring(1).replaceAll("[0-9]", "").matches("[^0-9]") || args.length < 2 || args[0].length() == 1) {
                plugin.sendMessage(player,"messages.syntax.city.bank.deposit");
                return;
            }

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
            return;
        }
        if (args[0].startsWith("-")) {
            if (args[0].substring(1).replaceAll("[0-9]", "").matches("[^0-9]") || args.length < 2 || args[0].length() == 1) {
                plugin.sendMessage(player,"messages.syntax.city.bank.withdraw");
                return;
            }

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
                plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",playerCity);
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
            return;
        }
        plugin.sendMessage(player,"messages.syntax.city.bank.bank");
        plugin.sendMessage(player,"messages.syntax.city.bank.deposit");
        plugin.sendMessage(player,"messages.syntax.city.bank.withdraw");
    }

    @Subcommand("new")
    public static void onNew(Player player, String cityName) {
        Economy economy = MetropolisRevamped.getEconomy();
        if (!player.hasPermission("metropolis.city.new")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getPlayerCityCount(player.getUniqueId().toString()) >= 3) {
            plugin.sendMessage(player,"messages.error.city.maxCityCount");
            return;
        }
        if (economy.getBalance(player) < MetropolisRevamped.configuration.getCityCreationCost()) {
            plugin.sendMessage(player,"messages.error.city.missing.balance.cityCost");
            return;
        }
        if (cityName.length() < 1 || cityName.length() > 16) {
            plugin.sendMessage(player,"messages.error.city.nameLength");
            return;
        }
        if (CityDatabase.cityExists(cityName)) {
            plugin.sendMessage(player,"messages.error.city.cityExists");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            plugin.sendMessage(player,"messages.error.city.claimExists");
            return;
        }
        CityDatabase.createCity(cityName,player.getUniqueId().toString(),player.getName(),player.getLocation());
        CityDatabase.createClaim(cityName,player.getLocation(),false,player.getUniqueId().toString(),player.getName());
        economy.withdrawPlayer(player,MetropolisRevamped.configuration.getCityCreationCost());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            plugin.sendMessage(onlinePlayer,"messages.city.successful.creation","%playername%",player.getDisplayName(),"%cityname%",cityName);
        }
    }

    @Subcommand("claim")
    public static void onClaim(Player player, @Optional int mass) {
        if (!player.hasPermission("metropolis.city.claim")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (!HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
            plugin.sendMessage(player,"messages.error.missing.homeCity");
            return;
        }
        String cityName = HCDatabase.getHomeCity(player.getUniqueId().toString());
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            plugin.sendMessage(player,"messages.error.city.claimExists");
            return;
        }
        if (Utilities.isCloseToOtherCity(player,player.getLocation())) {
            plugin.sendMessage(player,"messages.error.city.tooCloseToOtherCity");
            return;
        }
        if (CityDatabase.getCityRole(cityName,player.getUniqueId().toString()) == null || Objects.equals(CityDatabase.getCityRole(cityName, player.getUniqueId().toString()), "member") || Objects.equals(CityDatabase.getCityRole(cityName, player.getUniqueId().toString()), "inviter")) {
            plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",cityName);
            return;
        }
        CityDatabase.createClaim(cityName,player.getLocation(),false,player.getUniqueId().toString(),player.getName());
        CityDatabase.removeCityBalance(cityName,MetropolisRevamped.configuration.getCityClaimCost());
        plugin.sendMessage(player,"messages.city.successful.claim","%cityname%",cityName);
    }
}
