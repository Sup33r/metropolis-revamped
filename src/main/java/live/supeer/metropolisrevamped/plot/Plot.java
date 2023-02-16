package live.supeer.metropolisrevamped.plot;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.metropolisrevamped.Database;
import live.supeer.metropolisrevamped.Utilities;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Plot {

    private final int plotID;
    private final int cityID;
    private final String plotName;
    private String plotOwner;
    private String plotOwnerUUID;
    private final int plotYMin;
    private final int plotYMax;
    private String plotType;
    private final boolean kMarked;
    private boolean isForSale;
    private int plotPrice;
    private char[] permsMembers;
    private char[] permsOutsiders;
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
        this.permsMembers =  data.getString("plotPermsMembers") == null ? new char[0] : data.getString("plotPermsMembers").toCharArray();
        this.permsOutsiders =  data.getString("plotPermsOutsiders") == null ? new char[0] : data.getString("plotPermsOutsiders").toCharArray();
        this.plotCenter = Utilities.stringToLocation(data.getString("plotCenter"));
        this.plotFlags = data.getString("plotFlags");
        this.plotCreationDate = data.getInt("plotCreationDate");
        this.plotPoints = Utilities.stringToPolygon(data.getString("plotPoints"));
    }

    public void setPlotType(String plotType) {
        this.plotType = plotType;
        DB.executeUpdateAsync("UPDATE `mp_plots` SET `plotType` = " + Database.sqlString(plotType) + " WHERE `plotID` = " + plotID + ";");
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

    public void setPlotPerms(String type,String perms, String playerUUID) throws SQLException {
        if(type.equalsIgnoreCase("members")) {
            DB.executeUpdate("UPDATE `mp_plots` SET `plotPermsMembers` = " + Database.sqlString(perms) + " WHERE `plotID` = " + plotID + ";");
            this.permsMembers = perms.toCharArray();
            return;
        } else if(type.equalsIgnoreCase("outsiders")) {
            DB.executeUpdate("UPDATE `mp_plots` SET `plotPermsOutsiders` = " + Database.sqlString(perms) + " WHERE `plotID` = " + plotID + ";");
            this.permsOutsiders = perms.toCharArray();
            return;
        }
        DB.executeUpdate("INSERT INTO `mp_plotperms` (`plotId`, `cityId`, `plotPerms`, `playerUUID`, `playerName`) VALUES (" + plotID + ", " + cityID + ", " + Database.sqlString(perms) + ", " + Database.sqlString(playerUUID) + ", " + Database.sqlString(Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName()) + ") ON DUPLICATE KEY UPDATE plotPerms = '" + perms + "';");
    }

    public List<PlotPerms> getPlayerPlotPerms() {
        List<PlotPerms> plotPermsList = new ArrayList<>();
        try {
            List<DbRow> rows = DB.getResults("SELECT * FROM `mp_plotperms` WHERE `plotId` = " + plotID + ";");
            for (DbRow row : rows) {
                plotPermsList.add(new PlotPerms(row));
            }
            return plotPermsList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PlotPerms getPlayerPlotPerm(String playerUUID) {
        try {
            if (DB.getFirstRow("SELECT * FROM `mp_plotperms` WHERE `plotId` = " + plotID + " AND `playerUUID` = " + Database.sqlString(playerUUID) + ";") == null) {
                return null;
            }
            DbRow row = DB.getFirstRow("SELECT * FROM `mp_plotperms` WHERE `plotId` = " + plotID + " AND `playerUUID` = " + Database.sqlString(playerUUID) + ";");
            return new PlotPerms(row);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

}
