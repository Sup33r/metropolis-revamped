package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Objects;

@CommandAlias("hc")
public class CommandHomeCity extends BaseCommand {
    static MetropolisRevamped plugin;
    static CityDatabase cityDatabase;
    static HCDatabase homeCityDatabase;

    @Default
    public static void onHomeCity(Player player, @Optional String cityname) {
        if (!player.hasPermission("metropolis.homecity")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (cityname == null) {
            playerGui(player);
        } else {
            if (cityDatabase.cityExists(cityname)) {
                if (Objects.equals(cityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"mayor") || Objects.equals(cityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"vicemayor") || Objects.equals(cityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"assistant") || Objects.equals(cityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"inviter") || Objects.equals(cityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"member")) {
                    if (cityDatabase.getPlayerCityCount(player.getUniqueId().toString()) < 1) {
                        plugin.sendMessage(player,"messages.error.missing.membership");
                        return;
                    }
                    homeCityDatabase.setHomeCity(player.getUniqueId().toString(),cityname);
                    plugin.sendMessage(player,"messages.save.membership");
                } else {
                    plugin.sendMessage(player,"messages.error.missing.membership");
                }

            } else {
                plugin.sendMessage(player,"messages.error.missing.city");
            }
        }
    }

    private static void playerGui(Player player) {
        String[] cityNames = homeCityDatabase.homeCityList(player.getUniqueId().toString());
        if (cityDatabase.getPlayerCityCount(player.getUniqueId().toString()) < 1) {
            plugin.sendMessage(player,"messages.error.missing.membership");
            return;
        }
        if (cityNames.length == 1) {
            Inventory gui = Bukkit.createInventory(player, 9, "Homecity");
            for (int i = 0; i < cityNames.length; i++) {
                for (int j = 0; j < cityNames[i].length(); j++) {
                   if (j > 9) {
                       return;
                   }
                   ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[i].charAt(j)),cityNames[i]);
                    gui.setItem(i, item);
                }


            }
            player.openInventory(gui);
        }
    }

}
