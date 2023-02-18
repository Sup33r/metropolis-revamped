package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.city.Claim;
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
            if (HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
                plugin.sendMessage(player,"messages.error.missing.homeCity");
                return;
            }
            String homeCity = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());
            if (CityDatabase.getCity(homeCity).isEmpty()) {
                plugin.sendMessage(player,"messages.error.missing.city");
                return;
            }
            City city = CityDatabase.getCity(homeCity).get();
            String cityBalance = Utilities.formattedMoney(CityDatabase.getCityBalance(city));
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
            String playerCity = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());

            if (CityDatabase.getCity(cityName).isEmpty()) {
                plugin.sendMessage(player,"messages.error.missing.city");
                return;
            }
            City city = CityDatabase.getCity(cityName).get();

            if (economy.getBalance(player) < inputBalance) {
                plugin.sendMessage(player,"messages.error.missing.playerBalance","%cityname%",playerCity);
                return;
            }

            economy.withdrawPlayer(player,inputBalance);
            CityDatabase.addCityBalance(city,inputBalance);
            Database.addLogEntry(city,"{ \"type\": \"cityBank\", \"subtype\": \"deposit\", \"balance\": " + inputBalance + ", \"player\": " + player.getUniqueId().toString() + " }");
            plugin.sendMessage(player,"messages.city.successful.deposit","%amount%",Utilities.formattedMoney(inputBalance),"%cityname%",cityName);
            return;
        }
        if (args[0].startsWith("-")) {
            if (args[0].substring(1).replaceAll("[0-9]", "").matches("[^0-9]") || args.length < 2 || args[0].length() == 1) {
                plugin.sendMessage(player,"messages.syntax.city.bank.withdraw");
                return;
            }

            if (HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
                plugin.sendMessage(player,"messages.error.missing.homeCity");
                return;
            }
            int inputBalance = Integer.parseInt(args[0].replaceAll("[^0-9]",""));
            if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
                plugin.sendMessage(player,"messages.error.missing.city");
                return;
            }
            City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
            String inputBalanceFormatted = Utilities.formattedMoney(inputBalance);
            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            assert city != null;
            int cityBalance = city.getCityBalance();
            String cityRole = CityDatabase.getCityRole(city,player.getUniqueId().toString());

            if (cityRole == null || cityRole.equals("member") || cityRole.equals("inviter") || cityRole.equals("assistant")) {
                plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
                return;
            }


            if (!(reason.length() >= 8)) {
                plugin.sendMessage(player,"messages.error.missing.reasonLength","%cityname%",city.getCityName());
                return;
            }

            if (cityBalance <= 100000 || inputBalance > cityBalance - 100000) {
                plugin.sendMessage(player,"messages.error.missing.balance","%cityname%",city.getCityName());
                return;
            }

            CityDatabase.removeCityBalance(city,inputBalance);
            Database.addLogEntry(city,"{ \"type\": \"cityBank\", \"subtype\": \"withdraw\", \"balance\": " + inputBalance + ", \"player\": " + player.getUniqueId().toString() + ", \"reason\": \"" + reason + "\" }");
            economy.depositPlayer(player,inputBalance);
            plugin.sendMessage(player,"messages.city.successful.withdraw","%amount%",inputBalanceFormatted,"%cityname%",city.getCityName());
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
        if (CityDatabase.getCity(cityName).isPresent()) {
            plugin.sendMessage(player,"messages.error.city.cityExists");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            plugin.sendMessage(player,"messages.error.city.claimExists");
            return;
        }
        if (Utilities.isCloseToOtherCity(player,player.getLocation())) {
            plugin.sendMessage(player,"messages.error.city.tooCloseToOtherCity");
            return;
        }

        City city = CityDatabase.newCity(cityName,player);
        assert city != null;
        Database.addLogEntry(city,"{ \"type\": \"create\", \"subtype\": \"city\", \"name\": " + city.getCityName() + ", \"tax\": " + MetropolisRevamped.configuration.getCityStartingTax() + ", \"spawn\": " + Utilities.formatLocation(city.getCitySpawn()) + ", \"balance\": " + MetropolisRevamped.configuration.getCityStartingBalance() + ", \"player\": " + player.getUniqueId().toString() + " }");
        Database.addLogEntry(city,"{ \"type\": \"join\", \"subtype\": \"city\", \"player\": " + player.getUniqueId().toString() + " }");
        Database.addLogEntry(city,"{ \"type\": \"rank\", \"subtype\": \"change\", \"from\": " + "member" + ", \"to\": " + "mayor" + ", \"player\": " + player.getUniqueId().toString() + " }");
        Claim claim = CityDatabase.createClaim(city,player.getLocation(),false,player.getUniqueId().toString(),player.getName());
        assert claim != null;
        Database.addLogEntry(city,"{ \"type\": \"buy\", \"subtype\": \"claim\", \"balance\": " + "0" + ", \"claimlocation\": " + Utilities.formatChunk(claim.getClaimWorld(),claim.getXPosition(),claim.getZPosition()) + ", \"player\": " + player.getUniqueId().toString() + " }");
        economy.withdrawPlayer(player,MetropolisRevamped.configuration.getCityCreationCost());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            plugin.sendMessage(onlinePlayer,"messages.city.successful.creation","%playername%",player.getDisplayName(),"%cityname%",cityName);
        }
    }

    @Subcommand("claim")
    public static void onClaim(Player player, @Optional String mass) {
        if (!player.hasPermission("metropolis.city.claim")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
            plugin.sendMessage(player,"messages.error.missing.homeCity");
            return;
        }
        String cityName = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            plugin.sendMessage(player,"messages.error.city.claimExists");
            return;
        }
        if (CityDatabase.getCity(cityName).isEmpty()) {
            plugin.sendMessage(player,"messages.error.missing.city");
            return;
        }
        City city = CityDatabase.getCity(cityName).get();
        if (Utilities.isCloseToOtherCity(player,player.getLocation())) {
            plugin.sendMessage(player,"messages.error.city.tooCloseToOtherCity");
            return;
        }
        if (CityDatabase.getCityRole(city,player.getUniqueId().toString()) == null || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "member") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "inviter")) {
            plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",cityName);
            return;
        }
        CityDatabase.createClaim(city,player.getLocation(),false,player.getUniqueId().toString(),player.getName());
        CityDatabase.removeCityBalance(city,MetropolisRevamped.configuration.getCityClaimCost());
        plugin.sendMessage(player,"messages.city.successful.claim","%cityname%",cityName, "%amount%", Utilities.formattedMoney(MetropolisRevamped.configuration.getCityClaimCost()));
    }

    @Subcommand("set")
    public static void onSet(Player player, @Optional String[] args) {
        if (!player.hasPermission("metropolis.city.set")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (args.length == 0) {
            plugin.sendMessage(player,"messages.syntax.city.set.enter");
            plugin.sendMessage(player,"messages.syntax.city.set.exit");
            plugin.sendMessage(player,"messages.syntax.city.set.maxplotspermember");
            plugin.sendMessage(player,"messages.syntax.city.set.motd");
            plugin.sendMessage(player,"messages.syntax.city.set.name");
            plugin.sendMessage(player,"messages.syntax.city.set.spawn");
            plugin.sendMessage(player,"messages.syntax.city.set.tax");
            return;
        }

        if (args.length >= 2) {
            if (args[0].equals("enter")) {
                if (!player.hasPermission("metropolis.city.set.enter")) {
                    plugin.sendMessage(player,"messages.error.permissionDenied");
                    return;
                }
                if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
                    plugin.sendMessage(player,"messages.error.missing.homeCity");
                    return;
                }
                if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
                    plugin.sendMessage(player,"messages.error.missing.city");
                    return;
                }
                City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
                String cityRole = CityDatabase.getCityRole(city,player.getUniqueId().toString());
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                assert cityRole != null;
                if (cityRole.equals("assistant") || cityRole.equals("mayor") || cityRole.equals("vicemayor")) {
                    if (message.equals("-")) {
                        CityDatabase.setCityMessage(city,"enterMessage",null);
                        plugin.sendMessage(player,"messages.city.successful.set.enter.removed","%cityname%",city.getCityName());
                        return;
                    }
                    CityDatabase.setCityMessage(city,"enterMessage",message);
                    plugin.sendMessage(player,"messages.city.successful.set.enter.set","%cityname%",city.getCityName());
                } else {
                    plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
                }
            }
            if (args[0].equals("exit")) {
                if (!player.hasPermission("metropolis.city.set.exit")) {
                    plugin.sendMessage(player,"messages.error.permissionDenied");
                    return;
                }
                if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
                    plugin.sendMessage(player,"messages.error.missing.homeCity");
                    return;
                }
                if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
                    plugin.sendMessage(player,"messages.error.missing.city");
                    return;
                }
                City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
                String cityRole = CityDatabase.getCityRole(city,player.getUniqueId().toString());
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                assert cityRole != null;
                if (cityRole.equals("assistant") || cityRole.equals("mayor") || cityRole.equals("vicemayor")) {
                    if (message.equals("-")) {
                        CityDatabase.setCityMessage(city,"exitMessage",null);
                        plugin.sendMessage(player,"messages.city.successful.set.exit.removed","%cityname%",city.getCityName());
                        return;
                    }
                    CityDatabase.setCityMessage(city,"exitMessage",message);
                    plugin.sendMessage(player,"messages.city.successful.set.exit.set","%cityname%",city.getCityName());
                } else {
                    plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
                }
            }
            if (args[0].equals("motd")) {
                if (!player.hasPermission("metropolis.city.set.motd")) {
                    plugin.sendMessage(player,"messages.error.permissionDenied");
                    return;
                }
                if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
                    plugin.sendMessage(player,"messages.error.missing.homeCity");
                    return;
                }
                if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
                    plugin.sendMessage(player,"messages.error.missing.city");
                    return;
                }
                City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
                String cityRole = CityDatabase.getCityRole(city,player.getUniqueId().toString());
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                assert cityRole != null;
                if (cityRole.equals("assistant") || cityRole.equals("mayor") || cityRole.equals("vicemayor")) {
                    if (message.equals("-")) {
                        CityDatabase.setCityMessage(city,"motdMessage",null);
                        plugin.sendMessage(player,"messages.city.successful.set.motd.removed","%cityname%",city.getCityName());
                        return;
                    }
                    CityDatabase.setCityMessage(city,"motdMessage",message);
                    plugin.sendMessage(player,"messages.city.successful.set.motd.set","%cityname%",city.getCityName());
                } else {
                    plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
                }
            }
        }
    }

    @Subcommand("price")
    public static void onPrice(Player player) {
        if (player.hasPermission("metropolis.city.price")) {
            plugin.sendMessage(player,"messages.city.price","%city%",Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.creationCost")),"%chunk%",Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.claimcost")),"%bonus%",Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.bonuscost")),"%go%",Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.citygocost")),"%outpost%",Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.outpostcost")));
        } else {
            plugin.sendMessage(player,"messages.error.permissionDenied");
        }
    }
}