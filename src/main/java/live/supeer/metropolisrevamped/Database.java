package live.supeer.metropolisrevamped;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
            plugin.getLogger().warning("Failed to synchronize database, disabling plugin.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }
    public static boolean createTables() {
        try {

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_cities` (\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `originalMayorUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityBalance` int(25) NOT NULL,\n" +
                    " `citySpawn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    " `cityUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`cityName`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_members` (\n" +
                    "  `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityRole` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  `joinDate` bigint(30) DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`cityName`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_homecities` (\n" +
                    "  `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`playerUUID`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_claims` (\n" +
                    "  `claimId` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `world` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `xPosition` mediumint(9) NOT NULL,\n" +
                    "  `zPosition` mediumint(9) NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `minP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  `maxP` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  `spawn` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_events` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `date` bigint(30) DEFAULT NULL,\n" +
                    "  `track` int(11) DEFAULT NULL,\n" +
                    "  `state` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_heats` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `roundId` int(11) NOT NULL,\n" +
                    "  `heatNumber` int(11) NOT NULL,\n" +
                    "  `state` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `startTime` bigint(30) DEFAULT NULL,\n" +
                    "  `endTime` bigint(30) DEFAULT NULL,\n" +
                    "  `fastestLapUUID` varchar(255) COLLATE utf8mb4_unicode_ci NULL,\n" +
                    "  `totalLaps` int(11) DEFAULT NULL,\n" +
                    "  `totalPitstops` int(11) DEFAULT NULL,\n" +
                    "  `timeLimit` int(11) DEFAULT NULL,\n" +
                    "  `startDelay` int(11) DEFAULT NULL,\n" +
                    "  `maxDrivers` int(11) DEFAULT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_drivers` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `heatId` int(11) NOT NULL,\n" +
                    "  `position` int(11) NOT NULL,\n" +
                    "  `startPosition` int(11) NOT NULL,\n" +
                    "  `startTime` bigint(30) DEFAULT NULL,\n" +
                    "  `endTime` bigint(30) DEFAULT NULL,\n" +
                    "  `pitstops` int(11) DEFAULT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_laps` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `heatId` int(11) NOT NULL,\n" +
                    "  `trackId` int(11) NOT NULL,\n" +
                    "  `lapStart` bigint(30) DEFAULT NULL,\n" +
                    "  `lapEnd` bigint(30) DEFAULT NULL,\n" +
                    "  `pitted` tinyint(1) NOT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_locations` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `trackId` int(11) NOT NULL,\n" +
                    "  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `index` int(11) DEFAULT NULL,\n" +
                    "  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_points` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `regionId` int(11) NOT NULL,\n" +
                    "  `x` int(11) DEFAULT NULL,\n" +
                    "  `z` int(11) DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");


            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `ts_rounds` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `eventId` int(11) NOT NULL,\n" +
                    "  `roundIndex` int(11) NOT NULL DEFAULT 1,\n" +
                    "  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  `state` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `isRemoved` tinyint(4) NOT NULL DEFAULT 0,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n");

            return true;


        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
