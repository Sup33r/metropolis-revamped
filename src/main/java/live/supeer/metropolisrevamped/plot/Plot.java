package live.supeer.metropolisrevamped.plot;

import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Utilities;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class Plot {

    private final int plotID;
    private final int cityID;
    private final String plotName;
    private final String plotOwner;
    private final String plotOwnerUUID;
    private final int plotYMin;
    private final int plotYMax;
    private final String plotType;
    private final boolean kMarked;
    private final boolean isForSale;
    private final int plotPrice;
    private final String permsMembers;
    private final String permsOutsiders;
    private final Location plotCenter;
    private final String plotFlags;
    private final long plotCreationDate;
    private final Location[] plotPoints;

    public Plot(DbRow data) {
        this.plotID = data.getInt("plotId");
        this.cityID = data.getInt("cityId");
        this.plotName = data.getString("plotName");
        this.plotOwner = data.getString("plotOwner");
        this.plotOwnerUUID = data.getString("plotOwnerUUID");
        this.plotYMin = data.getInt("plotYMin");
        this.plotYMax = data.getInt("plotYMax");
        this.plotType = data.getString("plotType");
        this.kMarked = data.get("plotKMarked");
        this.isForSale = data.get("plotIsForSale");
        this.plotPrice = data.getInt("plotPrice");
        this.permsMembers = data.getString("permsMembers");
        this.permsOutsiders = data.getString("permsOutsiders");
        this.plotCenter = Utilities.stringToLocation(data.getString("plotCenter"));
        this.plotFlags = data.getString("plotFlags");
        this.plotCreationDate = data.getInt("plotCreationDate");
        this.plotPoints = new Location[] {Utilities.stringToLocation(data.getString("plotPoints"))};
    }


}
