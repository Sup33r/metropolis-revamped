package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.entity.Player;

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
            //Här uppe ska finaste grejen stå, nämligen den där gui saken, men den är svår, och skulle helst vilja ha en lib för det.
            player.sendMessage("Placeholder /homecity stadsnamn");
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


}
