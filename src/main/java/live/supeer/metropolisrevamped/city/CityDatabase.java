package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import live.supeer.metropolisrevamped.Utilities;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityDatabase {
    public static MetropolisRevamped plugin;
    private static final List<City> cities = new ArrayList<>();

    public static void initDBSync() throws SQLException {
        loadCities();
    }

    private static void loadCities() throws SQLException {
        var dbRows = DB.getResults("SELECT * FROM `mp_cities` WHERE `isRemoved` = 0;");

        for (DbRow dbRow : dbRows) {
            City city = new City(dbRow);
            cities.add(city);
            plugin.getLogger().info("Loaded city " + city.getCityName());
            loadMembers(city);
            loadClaims(city);
        }
    }

    private static void loadMembers(City rCity) throws SQLException {

        var members = DB.getResults("SELECT * FROM `mp_members` WHERE `cityName` = " + rCity.getCityName() + ";");
        for (DbRow member : members) {
            rCity.addCityMember(new Member(member));
        }
    }

    private static void loadClaims(City rCity) throws SQLException {
        var claims = DB.getResults("SELECT * FROM `mp_claims` WHERE `cityName` = " + rCity.getCityName() + ";");
        for (DbRow claim : claims) {
            rCity.addCityClaim(new Claim(claim));
        }
    }


    public static City newCity(String cityName, Player player) {
        try {
            DB.executeUpdateAsync("INSERT INTO `mp_cities` (`cityName`, `originalMayorName`, `originalMayorUUID`, `citySpawn`) VALUES (" + Database.sqlString(cityName) + ", " + Database.sqlString(player.getDisplayName()) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + Database.sqlString(Utilities.locationToString(player.getLocation())) + ");");
            City city = new City(DB.getFirstRow("SELECT * FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName) + ";"));
            cities.add(city);
            newMember(city, player);
            plugin.getLogger().info(player.getDisplayName() + " created a new city: " + cityName);
            return city;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void newMember(City city, Player player) {
        try {
            String cityName = city.getCityName();
            DB.executeUpdateAsync("INSERT INTO `mp_members` (`cityName`, `playerName`, `playerUUID`, `cityRole`) VALUES (" + Database.sqlString(cityName) + ", " + Database.sqlString(player.getDisplayName()) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", 'mayor');");
            city.addCityMember(new Member(DB.getFirstRow("SELECT * FROM `mp_members` WHERE `cityName` = " + Database.sqlString(cityName) + " AND `playerUUID` = " + Database.sqlString(player.getUniqueId().toString()) + ";")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCityRole(String cityName, String playerUUID) {
        try {
            if (DB.getResults("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(cityName) + ";").isEmpty()) return null;
            return DB.getFirstRow("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(cityName) + ";").getString("cityRole");
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

    public static void createCity(String cityName, String playerUUID, String playerName, Location spawnLocation) {
        try {
            DB.executeInsert("INSERT INTO `mp_cities` (`cityName`, `originalMayorUUID`, `originalMayorName`, `cityBalance`, `citySpawn`, `isRemoved`) VALUES (" + Database.sqlString(cityName) + ", " + Database.sqlString(playerUUID) + ", " + Database.sqlString(playerName) + ", " + MetropolisRevamped.configuration.getCityStartingBalance() + ", '" + Utilities.locationToString(spawnLocation) + "', " + 0 + ");");
            DB.executeInsert("INSERT INTO `mp_members` (`playerName`, `playerUUID`, `cityName`, `cityRole`) VALUES (" + Database.sqlString(playerName) + ", " + Database.sqlString(playerUUID) + ", " + Database.sqlString(cityName) + ", " + Database.sqlString("mayor") + ");");
            HCDatabase.setHomeCity(playerUUID, cityName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createClaim(String cityName, Location location, boolean outpost, String playername, String playerUUID) {
        int outpostCount = 0;
        if (outpost) {
            outpostCount = 1;
        }
        try {
            DB.executeInsert("INSERT INTO `mp_claims` (`claimerName`, `claimerUUID`, `world`, `xPosition`, `zPosition`, `cityName`, `outpost`) VALUES (" + Database.sqlString(playername) + ", " + Database.sqlString(playerUUID) + ", '" + location.getChunk().getWorld() + "', " + location.getChunk().getX() + ", " + location.getChunk().getZ() + ", '" + cityName + "', " + outpostCount + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String getClaim(Location location) {
        try {
            if (DB.getResults("SELECT * FROM `mp_claims` WHERE `world` = '" + location.getChunk().getWorld() + "' AND `xPosition` = " + location.getChunk().getX() + " AND `zPosition` = " + location.getChunk().getZ() + ";").isEmpty()) return null;
            return DB.getFirstRow("SELECT * FROM `mp_claims` WHERE `world` = '" + location.getChunk().getWorld() + "' AND `xPosition` = " + location.getChunk().getX() + " AND `zPosition` = " + location.getChunk().getZ() + ";").getString("cityName");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean hasClaim(int x, int z, World world) {
        try {
            return !DB.getResults("SELECT * FROM `mp_claims` WHERE `world` = '" + world + "' AND `xPosition` = " + x + " AND `zPosition` = " + z + ";").isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getCityMemberCount(String cityName) {
        try {
            return DB.getResults("SELECT * FROM `mp_members` WHERE `cityName` = " + Database.sqlString(cityName) + ";").size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
