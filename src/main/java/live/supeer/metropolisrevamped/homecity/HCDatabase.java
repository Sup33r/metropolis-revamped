package live.supeer.metropolisrevamped.homecity;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class HCDatabase {
    public static MetropolisRevamped plugin;

    public static void setHomeCity(String uuid, String cityname) {
        try {
            if (!hasHomeCity(uuid)) {
                DB.executeInsert("INSERT INTO mp_homecities (playerUUID, playerName, cityName) VALUES (" + Database.sqlString(uuid) + ", " + Database.sqlString(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName()) + ", " + Database.sqlString(cityname) + ");");
                return;
            }
            DB.executeUpdate("UPDATE mp_homecities SET cityName = " + Database.sqlString(cityname) + ", playerName = " + Database.sqlString(plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName()) + " WHERE playerUUID = " + Database.sqlString(uuid));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String getHomeCity(String uuid) {
        try {
            if (DB.getFirstRow("SELECT `cityName` FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").isEmpty()) return null;
            return DB.getFirstRow("SELECT `cityName` FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").getString("cityName");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean hasHomeCity(String uuid) {
        try {
            return !DB.getResults("SELECT * FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid)).isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
