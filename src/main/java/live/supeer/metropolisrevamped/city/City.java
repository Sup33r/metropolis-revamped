package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import live.supeer.metropolisrevamped.Utilities;
import lombok.Getter;
import org.bukkit.Location;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Getter
public class City {

    public static MetropolisRevamped plugin;

    private final int cityID;
    private String cityName;
    private final String originalMayorName;
    private final String originalMayorUUID;
    private Map<Member, City> cityMembers = new HashMap<>();
    private Map<Claim, City> cityClaims = new HashMap<>();
    private int cityBalance;
    private Location citySpawn;
    private final Timestamp cityCreationDate;
    private boolean isRemoved;

    public City(DbRow data) {
        this.cityID = data.getInt("cityID");
        this.cityName = data.getString("cityName");
        this.originalMayorName = data.getString("originalMayorName");
        this.originalMayorUUID = data.getString("originalMayorUUID");
        this.cityBalance = data.getInt("cityBalance");
        this.citySpawn = Utilities.stringToLocation(data.getString("citySpawn"));
        this.cityCreationDate = Timestamp.valueOf(data.getString("cityCreationDate"));
        this.isRemoved = data.getInt("isRemoved") != 0;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `cityName` = " + Database.sqlString(cityName) + " WHERE `cityID` = " + cityID + ";");
    }

    public void setCityBalance(int cityBalance) {
        this.cityBalance = cityBalance;
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `cityBalance` = " + cityBalance + " WHERE `cityID` = " + cityID + ";");
    }

    public void setCitySpawn(Location citySpawn) {
        this.citySpawn = citySpawn;
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `citySpawn` = " + Database.sqlString(Utilities.locationToString(citySpawn)) + " WHERE `cityID` = " + cityID + ";");
    }

    public void setCityStatus(boolean isRemoved) {
        this.isRemoved = isRemoved;
        DB.executeUpdateAsync("UPDATE `mp_cities` SET `isRemoved` = " + (isRemoved ? 1 : 0) + " WHERE `cityID` = " + cityID + ";");
    }

    public void addCityMember(Member member) {
        cityMembers.put(member, this);
    }

    public void addCityClaim(Claim claim) {
        cityClaims.put(claim, this);
    }

}
