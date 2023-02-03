package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.Database;

import java.sql.SQLException;

public class CityDatabase {
    public static boolean cityExists(String cityName) {
        try {
            return !DB.getResults("SELECT * FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName)).isEmpty();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return false;
    }

    public static String getCityRole(String cityName, String playerUUID) {
        try {
            return DB.getFirstRow("SELECT `cityRole` FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(cityName) + ";").getString("cityRole");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int getPlayerCityCount(String playerUUID) {
        try {
            return DB.getResults("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + ";").size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getCityBalance(String cityName) {
        try {
            return DB.getFirstRow("SELECT `cityBalance` FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName) + ";").getInt("cityBalance");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static void addCityBalance(String cityName, int amount) {
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `cityBalance` = `cityBalance` + " + amount + " WHERE `cityName` = " + Database.sqlString(cityName) + ";");
    }
    public static void removeCityBalance(String cityName, int amount) {
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `cityBalance` = `cityBalance` - " + amount + " WHERE `cityName` = " + Database.sqlString(cityName) + ";");
    }
    public static String[] memberCityList(String uuid) {
        try {
            return DB.getResults("SELECT `cityName` FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").stream().map(row -> row.getString("cityName")).toArray(String[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getCityName(String cityname) {
        try {
            return DB.getFirstRow("SELECT `cityName` FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityname) + ";").getString("cityName");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
