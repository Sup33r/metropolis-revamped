package live.supeer.metropolisrevamped.plot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.Utilities;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class Plot {

    private final int plotID;
    private final int cityID;
    private final String plotName;
    private String plotOwner;
    private String plotOwnerUUID;
    private final int plotYMin;
    private final int plotYMax;
    private final String plotType;
    private final boolean kMarked;
    private boolean isForSale;
    private int plotPrice;
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
        this.plotPoints = Utilities.stringToPolygon(data.getString("plotPoints"));
    }

    public void setPlotOwner(String plotOwner) {
        this.plotOwner = plotOwner;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotOwner` = " + Database.sqlString(plotOwner) + " WHERE `plotID` = " + plotID + ";");
    }

    public void setPlotOwnerUUID(String plotOwnerUUID) {
        this.plotOwnerUUID = plotOwnerUUID;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotOwnerUUID` = " + Database.sqlString(plotOwnerUUID) + " WHERE `plotID` = " + plotID + ";");
    }

    public void setForSale(boolean isForSale) {
        this.isForSale = isForSale;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotIsForSale` = " + isForSale + " WHERE `plotID` = " + plotID + ";");
    }

    public void setPlotPrice(int plotPrice) {
        this.plotPrice = plotPrice;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotPrice` = " + plotPrice + " WHERE `plotID` = " + plotID + ";");
    }

    public void removePlotOwner() {
        this.plotOwner = null;
        this.plotOwnerUUID = null;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotOwner` = NULL, `plotOwnerUUID` = NULL WHERE `plotID` = " + plotID + ";");
    }




}
