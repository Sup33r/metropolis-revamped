package live.supeer.metropolisrevamped;

import lombok.Getter;

@Getter
public class MetropolisRevampedConfiguration {

    private final String sqlHost;
    private final int sqlPort;
    private final String sqlDatabase;
    private final String sqlUsername;
    private final String sqlPassword;
    private final int cityCreationCost;
    private final int cityStartingBalance;
    private final int cityStartingTax;
    private final int cityGoCost;
    private final int cityClaimCost;
    private final int inviteCooldown;

    MetropolisRevampedConfiguration(MetropolisRevamped plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        sqlHost = plugin.getConfig().getString("sql.host");
        sqlPort = plugin.getConfig().getInt("sql.port");
        sqlDatabase = plugin.getConfig().getString("sql.database");
        sqlUsername = plugin.getConfig().getString("sql.username");
        sqlPassword = plugin.getConfig().getString("sql.password");
        inviteCooldown = plugin.getConfig().getInt("settings.cooldownTime.invite");
        cityCreationCost = plugin.getConfig().getInt("settings.city.creationcost");
        cityStartingBalance = plugin.getConfig().getInt("settings.city.startingbalance");
        cityStartingTax = plugin.getConfig().getInt("settings.city.startingtax");
        cityGoCost = plugin.getConfig().getInt("settings.city.gocost");
        cityClaimCost = plugin.getConfig().getInt("settings.city.claimcost");
    }
}
