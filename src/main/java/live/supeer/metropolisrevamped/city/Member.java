package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import lombok.Getter;

import java.sql.Timestamp;
@Getter
public class Member {
    public static MetropolisRevamped plugin;

    private String playerName;
    private final String playerUUID;
    private final String cityID;
    private String cityName;
    private String cityRole;
    private final Timestamp joinDate;

    public Member (DbRow data) {
        this.playerName = data.getString("playerName");
        this.playerUUID = data.getString("playerUUID");
        this.cityID = data.getString("cityID");
        this.cityName = data.getString("cityName");
        this.cityRole = data.getString("cityRole");
        this.joinDate = Timestamp.valueOf(data.getString("joinDate"));
    }

}

