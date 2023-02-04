package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Objects;

@CommandAlias("homecity|hc")
public class CommandHomeCity extends BaseCommand implements Listener {
    static MetropolisRevamped plugin;

    @Default
    public static void onHomeCity(Player player, @Optional String cityname) {
        if (!player.hasPermission("metropolis.homecity")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (cityname == null) {
            playerGui(player);
        } else {
            if (CityDatabase.cityExists(cityname)) {
                if (CityDatabase.getCityRole(cityname,player.getUniqueId().toString()) == null) {
                    plugin.sendMessage(player,"messages.error.missing.membership");
                    return;
                }
                if (Objects.equals(CityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"mayor") || Objects.equals(CityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"vicemayor") || Objects.equals(CityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"assistant") || Objects.equals(CityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"inviter") || Objects.equals(CityDatabase.getCityRole(cityname,player.getUniqueId().toString()),"member")) {
                    if (CityDatabase.getPlayerCityCount(player.getUniqueId().toString()) < 1) {
                        plugin.sendMessage(player,"messages.error.missing.homeCity");
                        return;
                    }
                    String realCityName = CityDatabase.getCityName(cityname);
                    HCDatabase.setHomeCity(player.getUniqueId().toString(),realCityName);
                    plugin.sendMessage(player,"messages.save.membership","%cityname%",realCityName);
                } else {
                    plugin.sendMessage(player,"messages.error.missing.membership");
                }

            } else {
                plugin.sendMessage(player,"messages.error.missing.city");
            }
        }
    }
    @EventHandler
    public void OnInventory(final InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Homecity")) {

            e.setCancelled(true);
            if (e.getCurrentItem() == null) {
                return;
            }
            if (e.getCurrentItem().getItemMeta() == null) {
                return;
            }
            if (!e.getCurrentItem().getType().equals(Material.WHITE_BANNER)) {
                return;
            }
            if (!e.getCurrentItem().getItemMeta().getDisplayName().startsWith("§")) {
                return;
            }

            String cityname = e.getCurrentItem().getItemMeta().getDisplayName().substring(4);
            Player player = (Player) e.getWhoClicked();

            HCDatabase.setHomeCity(player.getUniqueId().toString(),cityname);
            plugin.sendMessage(player,"messages.save.membership","%cityname%",cityname);
            player.closeInventory();
        }
    }

    private static void playerGui(Player player) {
        String[] cityNames = CityDatabase.memberCityList(player.getUniqueId().toString());
        if (cityNames == null) {
            plugin.sendMessage(player,"messages.error.missing.membership");
            return;
        }
        if (CityDatabase.getPlayerCityCount(player.getUniqueId().toString()) < 1) {
            plugin.sendMessage(player,"messages.error.missing.membership");
            return;
        }
        if (cityNames.length == 1) {
            Inventory gui = Bukkit.createInventory(player, 9, "§8Homecity");
                for (int j = 0; j < cityNames[0].length(); j++) {
                    if (j < 9) {
                        ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[0].charAt(j)), cityNames[0]);
                        gui.setItem(j, item);
                    }

            }
            player.openInventory(gui);
        }
        if (cityNames.length == 2) {
            Inventory gui = Bukkit.createInventory(player, 9+9, "§8Homecity");
            for (int i = 0; i < cityNames[0].length(); i++) {
                if (i < 9) {
                    ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[0].charAt(i)), cityNames[0]);
                    gui.setItem(i, item);
                }

            }
            for (int i = 0; i < cityNames[1].length(); i++) {
                if (i < 9) {
                    ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[1].charAt(i)), cityNames[1]);
                    gui.setItem(i+9, item);
                }
            }
            player.openInventory(gui);
        }
        if (cityNames.length == 3) {
            Inventory gui = Bukkit.createInventory(player, 9+9+9, "§8Homecity");
            for (int i = 0; i < cityNames[0].length(); i++) {
                if (i < 9) {
                    ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[0].charAt(i)), cityNames[0]);
                    gui.setItem(i, item);
                }

            }
            for (int i = 0; i < cityNames[1].length(); i++) {
                if (i < 9) {
                    ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[1].charAt(i)), cityNames[1]);
                    gui.setItem(i+9, item);
                }
            }
            for (int i = 0; i < cityNames[2].length(); i++) {
                if (i < 9) {
                    ItemStack item = Utilities.letterBanner(String.valueOf(cityNames[2].charAt(i)), cityNames[2]);
                    gui.setItem(i+18, item);
                }
            }
            player.openInventory(gui);
        }
    }
}
