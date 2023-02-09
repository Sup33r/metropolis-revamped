package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import live.supeer.metropolisrevamped.Utilities;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        var members = DB.getResults("SELECT * FROM `mp_members` WHERE `cityID` = '" + rCity.getCityID() + "';");
        for (DbRow member : members) {
            rCity.addCityMember(new Member(member));
        }
    }

    private static void loadClaims(City rCity) throws SQLException {
        String cityName = rCity.getCityName();
        Bukkit.broadcastMessage(cityName);
        var claims = DB.getResults("SELECT * FROM `mp_claims` WHERE `cityName` = '" + cityName + "';");
        for (DbRow claim : claims) {
            rCity.addCityClaim(new Claim(claim));
        }
    }


    public static City newCity(String cityName, Player player) {
        try {
            DB.executeUpdate("INSERT INTO `mp_cities` (`cityName`, `originalMayorUUID`, `originalMayorName`, `cityBalance`, `citySpawn`, `createDate`, `isRemoved`) VALUES (" + Database.sqlString(cityName) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + Database.sqlString(player.getDisplayName()) + ", " + MetropolisRevamped.configuration.getCityStartingBalance() + ", " + Database.sqlString(Utilities.locationToString(player.getLocation())) + ", " + Utilities.getTimestamp() + ", " + "0" + ");");
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
            DB.executeUpdate("INSERT INTO `mp_members` (`playerName`, `playerUUID`, `cityID`, `cityName`, `cityRole`, `joinDate`) VALUES (" + Database.sqlString(player.getDisplayName()) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + city.getCityID() + ", " + Database.sqlString(cityName) + ", " + "'mayor'" + ", " + Utilities.getTimestamp() + ");");
            city.addCityMember(new Member(DB.getFirstRow("SELECT * FROM `mp_members` WHERE `cityName` = " + Database.sqlString(cityName) + " AND `playerUUID` = " + Database.sqlString(player.getUniqueId().toString()) + ";")));
            HCDatabase.setHomeCity(player.getUniqueId().toString(), city);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void createClaim(City city, Location location, boolean outpost, String playername, String playerUUID) {
        try {
            String cityName = city.getCityName();
            DB.executeInsert("INSERT INTO `mp_claims` (`claimerName`, `claimerUUID`, `world`, `xPosition`, `zPosition`, `claimDate`, `cityName`, `outpost`) VALUES (" + Database.sqlString(playername) + ", " + Database.sqlString(playerUUID) + ", '" + location.getChunk().getWorld() + "', " + location.getChunk().getX() + ", " + location.getChunk().getZ() + ", " + Utilities.getTimestamp() + ", '" + cityName + "', " + outpost + ");");
            city.addCityClaim(new Claim(DB.getFirstRow("SELECT * FROM `mp_claims` WHERE `cityName` = " + Database.sqlString(cityName) + " AND `xPosition` = " + location.getChunk().getX() + " AND `zPosition` = " + location.getChunk().getZ() + ";")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Optional<City> getCity(String cityName) {
        for (City city : cities) {
            if (city.getCityName().equals(cityName)) return Optional.of(city);
        }
        return Optional.empty();
    }

    public static String getCityRole(City city, String playerUUID) {
        try {
            if (DB.getResults("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";").isEmpty()) return null;
            return DB.getFirstRow("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";").getString("cityRole");
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

    public static int getCityBalance(City city) {
        return city.getCityBalance();
    }
    public static void addCityBalance(City city, int amount) {
        city.addCityBalance(amount);
    }
    public static void removeCityBalance(City city, int amount) {
        city.removeCityBalance(amount);
    }
    public static String[] memberCityList(String uuid) {
        try {
            return DB.getResults("SELECT `cityName` FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(uuid) + ";").stream().map(row -> row.getString("cityName")).toArray(String[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public static String getCityMessage(City city, String messageType) {
        if (messageType.equals("enterMessage")) return city.getEnterMessage();
        if (messageType.equals("exitMessage")) return city.getExitMessage();
        if (messageType.equals("motdMessage")) return city.getMotdMessage();
        return null;
    }

    public static void setCityMessage(City city, String messageType, String message) {
        if (message == null) {
            if (messageType.equals("enterMessage")) city.setEnterMessage(null);
            if (messageType.equals("exitMessage")) city.setExitMessage(null);
            if (messageType.equals("motdMessage")) city.setMotdMessage(null);
            return;
        }
        if (messageType.equals("enterMessage")) city.setEnterMessage(message);
        if (messageType.equals("exitMessage")) city.setExitMessage(message);
        if (messageType.equals("motdMessage")) city.setMotdMessage(message);
    }
}
