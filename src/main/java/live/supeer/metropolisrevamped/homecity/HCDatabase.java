package live.supeer.metropolisrevamped.homecity;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class HCDatabase {
    public static MetropolisRevamped plugin;

    public static void setHomeCity(String uuid, City city) {
        try {
            if (hasHomeCity(uuid)) {
        DB.executeInsert(
            "INSERT INTO mp_homecities (playerUUID, playerName, cityName) VALUES ("
                + Database.sqlString(uuid)
                + ", "
                + Database.sqlString(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName())
                + ", "
                + Database.sqlString(city.getCityName())
                + ");");
                return;
            }
            DB.executeUpdate("UPDATE mp_homecities SET cityName = " + Database.sqlString(city.getCityName()) + ", playerName = " + Database.sqlString(plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName()) + " WHERE playerUUID = " + Database.sqlString(uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String getHomeCityToCityname(String uuid) {
        try {
            if (DB.getFirstRow("SELECT `cityName` FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").isEmpty()) return null;
            return DB.getFirstRow("SELECT `cityName` FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").getString("cityName");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static City getHomeCityToCity(String uuid) {
        try {
            String cityName = getHomeCityToCityname(uuid);
            if (CityDatabase.getCity(cityName).isEmpty()) return null;
            return CityDatabase.getCity(cityName).get();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean hasHomeCity(String uuid) {
        try {
            return DB.getResults("SELECT * FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid)).isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

}
