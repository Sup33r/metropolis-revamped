package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import live.supeer.metropolisrevamped.Utilities;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import live.supeer.metropolisrevamped.plot.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
            loadPlots(city);
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
            Claim claim1 = new Claim(claim);
            rCity.addCityClaim(claim1);
            plugin.getLogger().info("Loaded claim " + claim1.getXPosition() + " | " + claim1.getZPosition() + "  |  " + claim1.getClaimWorld() + " for city " + cityName);
        }
    }

    private static void loadPlots(City rCity) throws SQLException {
        String cityName = rCity.getCityName();
        Bukkit.broadcastMessage(cityName);
        var plots = DB.getResults("SELECT * FROM `mp_plots` WHERE `cityName` = '" + cityName + "';");
        for (DbRow plot : plots) {
            Plot plot1 = new Plot(plot);
            rCity.addCityPlot(plot1);
        }
    }


    public static City newCity(String cityName, Player player) {
        try {
            DB.executeUpdate("INSERT INTO `mp_cities` (`cityName`, `originalMayorUUID`, `originalMayorName`, `cityBalance`, `citySpawn`, `createDate`, `isRemoved`) VALUES (" + Database.sqlString(cityName) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + Database.sqlString(player.getName()) + ", " + MetropolisRevamped.configuration.getCityStartingBalance() + ", " + Database.sqlString(Utilities.locationToString(player.getLocation())) + ", " + Utilities.getTimestamp() + ", " + "0" + ");");
            City city = new City(DB.getFirstRow("SELECT * FROM `mp_cities` WHERE `cityName` = " + Database.sqlString(cityName) + ";"));
            cities.add(city);
            newMember(city, player);
            plugin.getLogger().info(player.getName() + " created a new city: " + cityName);
            return city;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void newMember(City city, Player player) {
        try {
            String cityName = city.getCityName();
            DB.executeUpdate("INSERT INTO `mp_members` (`playerName`, `playerUUID`, `cityID`, `cityName`, `cityRole`, `joinDate`) VALUES (" + Database.sqlString(player.getName()) + ", " + Database.sqlString(player.getUniqueId().toString()) + ", " + city.getCityID() + ", " + Database.sqlString(cityName) + ", " + "NULL" + ", " + Utilities.getTimestamp() + ");");
            city.addCityMember(
                    new Member(
                            DB.getFirstRow(
                                    "SELECT * FROM `mp_members` WHERE `cityName` = "
                                            + Database.sqlString(cityName)
                                            + " AND `playerUUID` = "
                                            + Database.sqlString(player.getUniqueId().toString())
                                            + ";")));
            HCDatabase.setHomeCity(player.getUniqueId().toString(), city);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Claim createClaim(City city, Location location, boolean outpost, String playername, String playerUUID) {
        try {
            String cityName = city.getCityName();
            DB.executeInsert("INSERT INTO `mp_claims` (`claimerName`, `claimerUUID`, `world`, `xPosition`, `zPosition`, `claimDate`, `cityName`, `outpost`) VALUES (" + Database.sqlString(playername) + ", " + Database.sqlString(playerUUID) + ", '" + location.getChunk().getWorld() + "', " + location.getChunk().getX() + ", " + location.getChunk().getZ() + ", " + Utilities.getTimestamp() + ", '" + cityName + "', " + outpost + ");");
            city.addCityClaim(new Claim(DB.getFirstRow("SELECT * FROM `mp_claims` WHERE `cityName` = " + Database.sqlString(cityName) + " AND `xPosition` = " + location.getChunk().getX() + " AND `zPosition` = " + location.getChunk().getZ() + ";")));
            return city.getCityClaim(location);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void newCityGo(Location location, String name, City city) {
        try {
            DB.executeInsert("INSERT INTO `mp_citygoes` (`cityID`, `goName`, `goLocation`, `createDate`) VALUES (" + city.getCityID() + ", " + Database.sqlString(name) + ", " + Database.sqlString(Utilities.locationToString(location)) + ", " + Utilities.getTimestamp() + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean cityGoExists(String name, City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
            return !results.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean memberExists(String player, City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_members` WHERE `cityID` = " + city.getCityID() + " AND `playerName` = " + Database.sqlString(player) + ";");
            return !results.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static int getCityGoCount(City city, String role) {
        if (role == null) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL ;");
                return results.size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "inviter")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter';");
                return results.size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "assistant")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant';");
                return results.size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "vicemayor")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant' OR `accessLevel` = 'vicemayor';");
                return results.size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "mayor")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant' OR `accessLevel` = 'vicemayor' OR `accessLevel` = 'mayor';");
                return results.size();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static List<String> getCityGoNames(City city, String role) {
        if (role == null) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL ;");
                return results.stream().map(result -> result.getString("goName")).collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "inviter")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter';");
                return results.stream().map(result -> result.getString("goName")).collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "assistant")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant';");
                return results.stream().map(result -> result.getString("goName")).collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "vicemayor")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant' OR `accessLevel` = 'vicemayor';");
                return results.stream().map(result -> result.getString("goName")).collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (Objects.equals(role, "mayor")) {
            try {
                var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `accessLevel` IS NULL OR `accessLevel` = 'inviter' OR `accessLevel` = 'assistant' OR `accessLevel` = 'vicemayor' OR `accessLevel` = 'mayor';");
                return results.stream().map(result -> result.getString("goName")).collect(Collectors.toList());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Location getCityGoLocation(String name, City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
            if (!results.isEmpty()) {
                return Utilities.stringToLocation(results.get(0).getString("goLocation"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCityGoAccessLevel(String name, City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
            if (!results.isEmpty()) {
                return results.get(0).getString("accessLevel");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setCityGoAccessLevel(String name, City city, String accessLevel) {
        try {
            DB.executeUpdate("UPDATE `mp_citygoes` SET `accessLevel` = " + Database.sqlString(accessLevel) + " WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setCityGoDisplayname(String name, City city, String displayname) {
        try {
            DB.executeUpdate("UPDATE `mp_citygoes` SET `goNickname` = " + Database.sqlString(displayname) + " WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setCityGoName(String name, City city, String newName) {
        try {
            DB.executeUpdate("UPDATE `mp_citygoes` SET `goName` = '" + Database.sqlString(newName) + "' WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCityGoDisplayname(String name, City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
            if (results.size() == 1) {
                return results.get(0).getString("goNickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteGo(String name, City city) {
        try {
            DB.executeUpdate("DELETE FROM `mp_citygoes` WHERE `cityID` = " + city.getCityID() + " AND `goName` = " + Database.sqlString(name) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Optional<City> getCity(String cityName) {
        for (City city : cities) {
            if (city.getCityName().equalsIgnoreCase(cityName)) return Optional.of(city);
        }
        return Optional.empty();
    }

    public static Optional<City> getCity(int id) {
        for (City city : cities) {
            if (city.getCityID() == id) return Optional.of(city);
        }
        return Optional.empty();
    }

    public static Claim getClaim(Location location) {
        for (City city : cities) {
            if (city.getCityClaim(location) != null) {
                return city.getCityClaim(location);
            }
        }
        return null;
    }

    public static City getCityByClaim(Location location) {
        for (City city : cities) {
            if (city.getCityClaim(location) != null) {
                return city;
            }
        }
        return null;
    }


    public static String getCityRole(City city, String playerUUID) {
        try {
            if (DB.getResults("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";").isEmpty())
                return null;
            return DB.getFirstRow("SELECT * FROM `mp_members` WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";").getString("cityRole");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setCityRole(City city, String playerUUID, String role) {
        try {
            DB.executeUpdate("UPDATE `mp_members` SET `cityRole` = " + Database.sqlString(role) + " WHERE `playerUUID` = " + Database.sqlString(playerUUID) + " AND `cityName` = " + Database.sqlString(city.getCityName()) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static boolean hasClaim(int x, int z, World world) {
        try {
            return !DB.getResults("SELECT * FROM `mp_claims` WHERE `world` = '" + world + "' AND `xPosition` = " + x + " AND `zPosition` = " + z + ";").isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public static String getCityMembers(City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_members` WHERE `cityName` = " + Database.sqlString(city.getCityName()) + ";");
            if (results.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            for (var row : results) {
                sb.append("§2").append(row.getString("playerName")).append("§a, ");
            }
            sb.delete(sb.length() - 2, sb.length());
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getCityMemberCount(City city) {
        try {
            return DB.getResults("SELECT * FROM `mp_members` WHERE `cityName` = " + Database.sqlString(city.getCityName()) + ";").size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void addCityBan(City city, String playerUUID, String reason, Player placer, long placeDate, long expiryDate) {
        try {
            DB.executeUpdate("INSERT INTO `mp_citybans` (`cityID`, `playerUUID`, `placeDate`, `expiryDate`, `reason`, `placeUUID`) VALUES (" + city.getCityID() + ", " + Database.sqlString(playerUUID) + ", " + placeDate + ", " + expiryDate + ", " + Database.sqlString(reason) + ", " + Database.sqlString(placer.getUniqueId().toString()) + ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<Ban> getCityBans(City city) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citybans` WHERE `cityID` = " + city.getCityID() + ";");
            if (results.isEmpty()) return null;
            List<Ban> bans = new ArrayList<>();
            for (var row : results) {
                bans.add(new Ban(row));
            }
            return bans;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Ban getCityBan(City city, String playerUUID) {
        try {
            var results = DB.getResults("SELECT * FROM `mp_citybans` WHERE `cityID` = " + city.getCityID() + " AND `playerUUID` = " + Database.sqlString(playerUUID) + ";");
            if (results.isEmpty()) return null;
            var row = results.get(0);
            return new Ban(row);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void removeCityBan(City city, String playerUUID) {
        try {
            DB.executeUpdate("DELETE FROM `mp_citybans` WHERE `cityID` = " + city.getCityID() + " AND `playerUUID` = " + Database.sqlString(playerUUID) + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
