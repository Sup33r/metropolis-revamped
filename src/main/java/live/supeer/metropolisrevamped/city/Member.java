package live.supeer.metropolisrevamped.city;

import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.MetropolisRevamped;
import lombok.Getter;

@Getter
public class Member {
  public static MetropolisRevamped plugin;

  private final String playerName;
  private final String playerUUID;
  private final int cityID;
  private final String cityName;
  private final String cityRole;
  private final long joinDate;

  public Member(DbRow data) {
    this.playerName = data.getString("playerName");
    this.playerUUID = data.getString("playerUUID");
    this.cityID = data.getInt("cityID");
    this.cityName = data.getString("cityName");
    this.cityRole = data.getString("cityRole");
    this.joinDate = data.getInt("joinDate");
  }
}
