package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Objects;

@CommandAlias("plot")
public class CommandPlot extends BaseCommand  {
    static MetropolisRevamped plugin;

    @Default
    public static void onPlot(Player player) {

    }

    @Subcommand("new")
    public static void onNew(Player player, String[] args) {
        if (!player.hasPermission("metropolis.plot.new")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (!MetropolisListener.playerPolygons.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player,"messages.error.missing.plot");
            return;
        }
        if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
            plugin.sendMessage(player,"messages.error.missing.city");
        }
        City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
        if (Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "member") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "inviter")) {
            plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
            return;
        }
        Polygon regionPolygon = MetropolisListener.playerPolygons.get(player.getUniqueId());
        double minX = regionPolygon.getBounds().getMinX();
        double maxX = regionPolygon.getBounds().getMaxX();
        double minY = regionPolygon.getBounds().getMinY();
        double maxY = regionPolygon.getBounds().getMaxY();

        int chunkSize = 16;
        int startX = (int) Math.floor(minX / chunkSize) * chunkSize;
        int endX = (int) Math.floor(maxX / chunkSize) * chunkSize + chunkSize;
        int startY = (int) Math.floor(minY / chunkSize) * chunkSize;
        int endY = (int) Math.floor(maxY / chunkSize) * chunkSize + chunkSize;

        Rectangle chunkBounds = new Rectangle();
        for (int x = startX; x < endX; x += chunkSize) {
            for (int z = startY; z < endY; z += chunkSize) {
                chunkBounds.setBounds(x, z, chunkSize, chunkSize);
                if (regionPolygon.intersects(chunkBounds)) {
                    if (CityDatabase.getClaim(new Location(player.getWorld(),x,0,z)) == null || !Objects.equals(Objects.requireNonNull(CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z))).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                        player.sendMessage("§cEn markbit inom din markering är inte i din stad.");
                        return;
                    }

                }
            }
        }

    }

    @Subcommand("delete")
    public static void onDelete(Player player) {

    }

    @Subcommand("info")
    public static void onInfo(Player player) {

    }

    @Subcommand("player")
    public static void onPlayer(Player player) {

    }


}
