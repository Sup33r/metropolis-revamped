package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;

import java.sql.SQLException;

public class CityDatabase {
    public boolean cityExists(String cityName) {
        try {
            return DB.getResults("SELECT `*` FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName)).isEmpty();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return false;
    }

    public String getCityRole(String cityName, String playerUUID) {
        try {
            return DB.getFirstRow("SELECT `cityRole` FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(cityName) + ";").getString("cityRole");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public int getPlayerCityCount(String playerUUID) {
        try {
            return DB.getResults("SELECT `*` FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + ";").size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getCityBalance(String cityName) {
        try {
            return DB.getFirstRow("SELECT `cityBalance` FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName) + ";").getInt("cityBalance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
