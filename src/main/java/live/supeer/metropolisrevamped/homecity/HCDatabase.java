package live.supeer.metropolisrevamped.homecity;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;

import java.sql.SQLException;
import java.util.UUID;

public class HCDatabase {
    private static MetropolisRevamped plugin;

    public void setHomeCity(String uuid, String cityname) {
        try {
            if (getHomeCity(uuid) == null) {
                DB.executeInsert("INSERT INTO `mp_homecities` " + "(`playerUUID`, `playerName`, `cityName`) " + "VALUES('" + uuid + "', " + Database.sqlString(plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).toString()) + ", " + Database.sqlString(cityname) + ");");
                return;
            }
            DB.executeUpdateAsync("UPDATE `mp_homecities` SET `cityName` = " + Database.sqlString(cityname) + "AND `playerName` = " + Database.sqlString(plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).toString()) + " WHERE `playerUUID` = " + Database.sqlString(uuid) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getHomeCity(String uuid) {
        try {
            return DB.getFirstRow("SELECT `cityName` FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").getString("cityName");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void removeHomeCity(String uuid) {
        DB.executeUpdateAsync("DELETE FROM `mp_homecities` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";");
    }
}
