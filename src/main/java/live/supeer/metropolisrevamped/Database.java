package live.supeer.metropolisrevamped;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;

import java.sql.SQLException;

public class Database {

    public static MetropolisRevamped plugin;

    public static boolean initialize() {
        try {
            BukkitDB.createHikariDatabase(MetropolisRevamped.getPlugin(),
                    MetropolisRevamped.configuration.getSqlUsername(),
                    MetropolisRevamped.configuration.getSqlPassword(),
                    MetropolisRevamped.configuration.getSqlDatabase(),
                    MetropolisRevamped.configuration.getSqlHost() + ":" + MetropolisRevamped.configuration.getSqlPort()
            );
            return createTables();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize database, disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public static boolean synchronize() {
        try {

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            plugin.getLogger().warning("Failed to synchronize with database, disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }
    public static String sqlString(String string) {
        return string == null ? "NULL" : "'" + string.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
    public static boolean createTables() {
        try {

            DB.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mp_cities` (
                      `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `originalMayorUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `originalMayorName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `cityBalance` int(25) NOT NULL,
                     `citySpawn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                     `cityUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `isRemoved` tinyint(1) NOT NULL,
                      PRIMARY KEY (`cityName`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;""");

            DB.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mp_members` (
                      `playerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `cityRole` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                      `joinDate` bigint(30) DEFAULT NULL,
                      PRIMARY KEY (`cityName`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");

            DB.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mp_homecities` (
                      `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `playerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `cityName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                      PRIMARY KEY (`playerUUID`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");

            DB.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `mp_claims` (
                      `claimId` int(11) NOT NULL AUTO_INCREMENT,
                      `claimerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `claimerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `world` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `xPosition` mediumint(9) NOT NULL,
                      `zPosition` mediumint(9) NOT NULL,
                      `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                      `claimDate` bigint(30) DEFAULT NULL,
                      `outpost` tinyint(1) DEFAULT '0',
                      PRIMARY KEY (`claimId`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
            return true;


        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
