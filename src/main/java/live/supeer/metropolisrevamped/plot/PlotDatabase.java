package live.supeer.metropolisrevamped.plot;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.Utilities;
import live.supeer.metropolisrevamped.city.City;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;

public class PlotDatabase {

    public static Plot createPlot(Player player, Location[] plotPoints, String plotName, City city) {
        if (plotName == null) {
            plotName = "Tomt #" + getPlotAmount()+1;
        }
        Polygon plotPolygon = new Polygon();
        for (Location plotPoint : plotPoints) {
            plotPolygon.addPoint(plotPoint.getBlockX(), plotPoint.getBlockZ());
        }
        int centerX = plotPolygon.getBounds().x + plotPolygon.getBounds().width / 2;
        int centerZ = plotPolygon.getBounds().y + plotPolygon.getBounds().height / 2;
        Location plotCenter = new Location(plotPoints[0].getWorld(), centerX, player.getWorld().getHighestBlockYAt(centerX, centerZ), centerZ);
        try {
            DB.executeUpdate("INSERT INTO `mp_plots` (`cityId`, `cityName`, `plotName`, `plotOwner`, `plotOwnerUUID`, `plotPoints`, `plotType`, `plotPermsMembers`, `plotPermsOutsiders`, `plotCenter`, `plotCreationDate`) VALUES (" + city.getCityID() + ", " + Database.sqlString(city.getCityName()) + ", " + Database.sqlString(plotName) + ", " + Database.sqlString(player.getDisplayName()) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + Database.sqlString(Utilities.polygonToString(plotPoints)) + ", " + "gt" + ", " + "gt" + ", " + Database.sqlString(Utilities.locationToString(plotCenter)) + ", " + Utilities.getTimestamp() + ");");
            Plot plot = new Plot(DB.getFirstRow("SELECT * FROM `mp_plots` WHERE `plotName` = " + Database.sqlString(plotName) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";"));
            city.addCityPlot(plot);
            return plot;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getPlotAmount() {
        try {
            return DB.getResults("SELECT * FROM `mp_plots`").size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
