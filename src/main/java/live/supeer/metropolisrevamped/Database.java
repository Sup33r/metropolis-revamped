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
            plugin.getLogger().warning("Anslutningen till databasen misslyckades, avslutar pluginet.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public static boolean synchronize() {
        try {

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            plugin.getLogger().warning("Misslyckades med att synkronisera databasen, avslutar pluginet.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }
    public static String sqlString(String string) {
        return string == null ? "NULL" : "'" + string.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
    public static boolean createTables() {
        try {

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_cities` (\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `originalMayorUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `originalMayorName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityBalance` int(25) NOT NULL,\n" +
                    " `citySpawn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    " `cityUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `isRemoved` tinyint(1) NOT NULL,\n" +
                    "  PRIMARY KEY (`cityName`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_members` (\n" +
                    "  `playerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityRole` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  `joinDate` bigint(30) DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`cityName`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_homecities` (\n" +
                    "  `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `playerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`playerUUID`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");

            DB.executeUpdate("CREATE TABLE IF NOT EXISTS `mp_claims` (\n" +
                    "  `claimId` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `claimerName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `claimerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `world` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `xPosition` mediumint(9) NOT NULL,\n" +
                    "  `zPosition` mediumint(9) NOT NULL,\n" +
                    "  `cityName` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
                    "  `claimDate` bigint(30) DEFAULT NULL,\n" +
                    "  `outpost` tinyint(1) DEFAULT '0',\n" +
                    "  PRIMARY KEY (`claimId`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;");
            return true;


        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}
