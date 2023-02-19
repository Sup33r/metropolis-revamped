package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import live.supeer.metropolisrevamped.plot.Plot;
import live.supeer.metropolisrevamped.plot.PlotDatabase;
import live.supeer.metropolisrevamped.plot.PlotPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@CommandAlias("plot")
public class CommandPlot extends BaseCommand {
    static MetropolisRevamped plugin;

    @Default
    public static void onPlot(Player player) {

    }

    @Subcommand("new")
    public static void onNew(Player player, @Optional String plotname) {
        if (!player.hasPermission("metropolis.plot.new")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (!MetropolisListener.playerPolygons.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player, "messages.error.missing.plot");
            return;
        }
        if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
            plugin.sendMessage(player, "messages.error.missing.city");
        }
        City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
        if (Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "member") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "inviter")) {
            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
            return;
        }
        Polygon regionPolygon = MetropolisListener.playerPolygons.get(player.getUniqueId());
        Location[] locations = MetropolisListener.savedLocs.get(player.getUniqueId()).toArray(new Location[0]);
        double minX = regionPolygon.getBounds().getMinX();
        double maxX = regionPolygon.getBounds().getMaxX();
        double minY = regionPolygon.getBounds().getMinY();
        double maxY = regionPolygon.getBounds().getMaxY();

        if (maxX - minX < 3 || maxY - minY < 3) {
            plugin.sendMessage(player, "messages.error.plot.tooSmall");
            return;
        }
        if (MetropolisListener.playerYMax.get(player.getUniqueId()) - MetropolisListener.playerYMin.get(player.getUniqueId()) < 3) {
            plugin.sendMessage(player, "messages.error.plot.tooLowY");
            return;
        }

        int chunkSize = 16;
        int startX = (int) Math.floor(minX / chunkSize) * chunkSize;
        int endX = (int) Math.floor(maxX / chunkSize) * chunkSize + chunkSize;
        int startY = (int) Math.floor(minY / chunkSize) * chunkSize;
        int endY = (int) Math.floor(maxY / chunkSize) * chunkSize + chunkSize;

        Rectangle chunkBounds = new Rectangle();
        for (int x = startX; x < endX; x += chunkSize) {
            for (int z = startY; z < endY; z += chunkSize) {
                chunkBounds.setBounds(x, z, chunkSize, chunkSize);
                if (regionPolygon.intersects(chunkBounds)) {
                    if (CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z)) == null || !Objects.equals(Objects.requireNonNull(CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z))).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                        plugin.sendMessage(player, "messages.error.plot.intersectsExistingClaim");
                        return;
                    }
                    if (PlotDatabase.intersectsExistingPlot(regionPolygon, city)) {
                        plugin.sendMessage(player, "messages.error.plot.intersectsExistingPlot");
                        return;
                    }
                    Plot plot = PlotDatabase.createPlot(player, locations, plotname, city, MetropolisListener.playerYMin.get(player.getUniqueId()), MetropolisListener.playerYMax.get(player.getUniqueId()));
                    assert plot != null;
                        Database.addLogEntry(city,"{ \"type\": \"create\", \"subtype\": \"plot\", \"id\": " + plot.getPlotID() + ", \"name\": " + plotname + ", \"points\": " + Utilities.parsePoints(locations) + ", \"ymin\": " + MetropolisListener.playerYMin.get(player.getUniqueId()) + ", \"ymax\": " + MetropolisListener.playerYMax.get(player.getUniqueId()) + ", \"player\": " + player.getUniqueId().toString() + " }");
                    MetropolisListener.savedLocs.remove(player.getUniqueId());
                    MetropolisListener.playerPolygons.remove(player.getUniqueId());
                    MetropolisListener.playerYMin.remove(player.getUniqueId());
                    MetropolisListener.playerYMax.remove(player.getUniqueId());
                    plugin.sendMessage(player, "messages.city.successful.set.plot.new", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                }
            }
        }

    }

    @Subcommand("expand")
    public static void onExpand(Player player, String[] args) {
        if (!player.hasPermission("metropolis.plot.expand")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (!MetropolisListener.playerPolygons.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player, "messages.error.missing.plot");
            return;
        }

        if (args.length == 0) {
            MetropolisListener.playerYMin.put(player.getUniqueId(), -64);
            MetropolisListener.playerYMax.put(player.getUniqueId(), 319);
            plugin.sendMessage(player, "messages.city.successful.set.plot.expand.max");
            return;
        }
        if (args.length > 2) {
            plugin.sendMessage(player, "messages.syntax.plot.expand");
            return;
        }
        if (args.length == 1) {
            if (args[0].equals("up")) {
                MetropolisListener.playerYMax.put(player.getUniqueId(), 319);
                plugin.sendMessage(player, "messages.city.successful.set.plot.expand.up.max");
                return;
            } else if (args[0].equals("down")) {
                MetropolisListener.playerYMin.put(player.getUniqueId(), -64);
                plugin.sendMessage(player, "messages.city.successful.set.plot.expand.down.max");
                return;
            } else {
                plugin.sendMessage(player, "messages.syntax.plot.expand");
                return;
            }
        }
        if (args[0].matches("[0-9]")) {
            if (args[1].equals("up")) {
                if (MetropolisListener.playerYMax.get(player.getUniqueId()) + Integer.parseInt(args[0]) > 319) {
                    plugin.sendMessage(player, "messages.error.plot.tooLowExpand");
                    return;
                }
                MetropolisListener.playerYMax.put(player.getUniqueId(), MetropolisListener.playerYMax.get(player.getUniqueId()) + Integer.parseInt(args[0]));
                plugin.sendMessage(player, "messages.city.successful.set.plot.expand.up", "%y%", args[0]);
            } else if (args[1].equals("down")) {
                if (MetropolisListener.playerYMin.get(player.getUniqueId()) - Integer.parseInt(args[0]) < -64) {
                    plugin.sendMessage(player, "messages.error.plot.tooHighExpand");
                    return;
                }
                MetropolisListener.playerYMin.put(player.getUniqueId(), MetropolisListener.playerYMin.get(player.getUniqueId()) - Integer.parseInt(args[0]));
                plugin.sendMessage(player, "messages.city.successful.set.plot.expand.down", "%y%", args[0]);
            } else {
                plugin.sendMessage(player, "messages.syntax.plot.expand");
            }
        } else {
            plugin.sendMessage(player, "messages.syntax.plot.expand");
        }
    }

    @Subcommand("delete")
    public static void onDelete(Player player) {
        if (!player.hasPermission("metropolis.plot.delete")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
        String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
        assert role != null;
        boolean isAssistant = role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location location : plot.getPlotPoints()) {
                    polygon.addPoint(location.getBlockX(), location.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (!isAssistant) {
                        plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                        return;
                    }
                    Database.addLogEntry(city,"{ \"type\": \"delete\", \"subtype\": \"plot\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"player\": " + player.getUniqueId().toString() + " }");
                    PlotDatabase.deletePlot(plot);
                    plugin.sendMessage(player, "messages.city.successful.set.delete.plot", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                    return;
                }
            }
        }
    }

    @Subcommand("leave")
    public static void onLeave(Player player) {
        if (!player.hasPermission("metropolis.plot.leave")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            for (Plot plot : city.getCityPlots()) {
                if (plot.getPlotOwnerUUID() == null) {
                    plugin.sendMessage(player, "messages.error.plot.notOwner");
                    return;
                }
                Polygon polygon = new Polygon();
                for (Location location : plot.getPlotPoints()) {
                    polygon.addPoint(location.getBlockX(), location.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (plot.getPlotOwnerUUID().equals(player.getUniqueId().toString())) {
                        plot.removePlotOwner();
                        Database.addLogEntry(city,"{ \"type\": \"leave\", \"subtype\": \"plot\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"player\": " + player.getUniqueId().toString() + " }");
                        plugin.sendMessage(player, "messages.city.successful.set.plot.leave", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                    } else {
                        plugin.sendMessage(player, "messages.error.plot.set.owner.notOwner");
                    }
                    return;
                }
            }
        }
    }

    @Subcommand("market")
    public static void onMarket(Player player, String arg) {
        if (!player.hasPermission("metropolis.plot.market")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
            return;
        }
        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location location : plot.getPlotPoints()) {
                    polygon.addPoint(location.getBlockX(), location.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (Objects.equals(plot.getPlotOwnerUUID(), player.getUniqueId().toString()) || Objects.equals(role, "inviter") || Objects.equals(role, "assistant") || Objects.equals(role, "vicemayor") || Objects.equals(role, "mayor")) {
                        if (arg.equals("-")) {
                            plot.setForSale(false);
                            plugin.sendMessage(player, "messages.city.successful.set.plot.market.remove", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                            return;
                        }
                        if (arg.matches("[0-9]")) {
                            if (plot.isForSale()) {
                                if (plot.getPlotPrice() == Integer.parseInt(arg)) {
                                    plugin.sendMessage(player, "messages.error.plot.market.noChange", "%cityname%", city.getCityName());
                                    return;
                                }
                                plugin.sendMessage(player, "messages.city.successful.set.plot.market.change", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName(),"%from%",""+plot.getPlotPrice(), "%to%", arg);
                                Database.addLogEntry(city,"{ \"type\": \"plotMarket\", \"subtype\": \"change\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotPrice() + ", \"to\": " + arg + ", \"player\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotPrice(Integer.parseInt(arg));
                                return;
                            }
                            plot.setForSale(true);
                            if (plot.getPlotPrice() == Integer.parseInt(arg)) {
                                plugin.sendMessage(player, "messages.error.plot.market.noChange", "%cityname%", city.getCityName());
                                return;
                            }
                            plot.setPlotPrice(Integer.parseInt(arg));
                            Database.addLogEntry(city,"{ \"type\": \"plotMarket\", \"subtype\": \"add\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"to\": " + arg + ", \"player\": " + player.getUniqueId().toString() + " }");
                            plugin.sendMessage(player, "messages.city.successful.set.plot.market.set", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName(), "%amount%", arg);
                            return;
                        }
                        plugin.sendMessage(player, "messages.syntax.plot.market");
                    } else {
                        plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                    }
                    return;
                }
            }
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
        } else {
            plugin.sendMessage(player, "messages.error.plot.notInPlot");
        }
    }

    @Subcommand("info")
    public static void onInfo(Player player) {
        if (!player.hasPermission("metropolis.plot.info")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            player.sendMessage(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName());
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }
        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location loc : plot.getPlotPoints()) {
                    polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    List<Player> players = new ArrayList<>();
                    plugin.sendMessage(player, "messages.plot.list.header", "%plot%", plot.getPlotName());
                    plugin.sendMessage(player, "messages.plot.list.id", "%id%", String.valueOf(plot.getPlotID()));
                    plugin.sendMessage(player, "messages.plot.list.city", "%cityname%", city.getCityName());
                    plugin.sendMessage(player, "messages.plot.list.owner", "%owner%", plot.getPlotOwner());
                    plugin.sendMessage(player, "messages.plot.list.pvp", "%status%", "placeholder");
                    plugin.sendMessage(player, "messages.plot.list.animals", "%status%", "placeholder");
                    plugin.sendMessage(player, "messages.plot.list.monsters", "%status%", "placeholder");
                    plugin.sendMessage(player, "messages.plot.list.locked", "%status%", "placeholder");
                    if (plot.isKMarked()) {
                        plugin.sendMessage(player, "messages.plot.list.k-marked", "%status%", "§cJa");
                    } else {
                        plugin.sendMessage(player, "messages.plot.list.k-marked", "%status%", "§aNej");
                    }
                    plugin.sendMessage(player, "messages.plot.list.lose.items", "%status%", "placeholder");
                    plugin.sendMessage(player, "messages.plot.list.lose.xp", "%status%", "placeholder");
                    if (player.hasPermission("metropolis.plot.info.coordinates") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "assistant") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "vicemayor") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "mayor")) {
                        plugin.sendMessage(player, "messages.plot.list.middle", "%world%", plot.getPlotCenter().getWorld().getName(), "%x%", String.valueOf(plot.getPlotCenter().getBlockX()), "%y%", String.valueOf(plot.getPlotCenter().getBlockY()), "%z%", String.valueOf(plot.getPlotCenter().getBlockZ()));
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        for (Plot plot1 : city.getCityPlots()) {
                            Polygon polygon1 = new Polygon();
                            for (Location loc : plot1.getPlotPoints()) {
                                polygon1.addPoint(loc.getBlockX(), loc.getBlockZ());
                            }
                            if (polygon1.contains(p.getLocation().getBlockX(), p.getLocation().getBlockZ())) {
                                if (plot1.getPlotID() == plot.getPlotID()) {
                                    players.add(p);
                                }
                            }
                        }
                    }
                    if (players.size() > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Player p : players) {
                            //dont append last player
                            if (players.indexOf(p) == players.size() - 1)
                                stringBuilder.append(p.getName()).append("§2");
                            else
                                stringBuilder.append(p.getName()).append("§2,§a ");
                        }
                        plugin.sendMessage(player, "messages.plot.list.players", "%players%", stringBuilder.substring(0, stringBuilder.toString().length() - 2));
                    }
                }
            }
        } else {
            plugin.sendMessage(player, "messages.error.plot.notFound");
        }
    }

    @Subcommand("player")
    public static void onPlayer(Player player, String playerName) {
        if (!player.hasPermission("metropolis.plot.player")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        @Deprecated
        Player p = Bukkit.getOfflinePlayer(playerName).getPlayer();
        if (p == null) {
            plugin.sendMessage(player, "messages.error.player.notFound");
            return;
        }
        if (CityDatabase.memberCityList(p.getUniqueId().toString()) == null || Objects.requireNonNull(CityDatabase.memberCityList(p.getUniqueId().toString())).length == 0) {
            plugin.sendMessage(player, "messages.error.city.notInCity");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String c : Objects.requireNonNull(CityDatabase.memberCityList(p.getUniqueId().toString()))) {
            if (CityDatabase.getCity(c).isEmpty()) {
                return;
            }
            City city = CityDatabase.getCity(c).get();
            for (Plot plot : city.getCityPlots()) {
                if (plot.getPlotOwner().equals(p.getName())) {
                    stringBuilder.append("§a").append(plot.getPlotName()).append("§2,§a ");
                }
            }
            if (stringBuilder.toString().length() == 0) {
                return;
            }
            player.sendMessage("§a§l" + city.getCityName() + "§2: §a" + stringBuilder.substring(0, stringBuilder.toString().length()));
        }
    }

    @Subcommand("tp")
    public static void onTp(Player player, String[] args) {
        if (!player.hasPermission("metropolis.plot.tp")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (args.length != 1) {
            plugin.sendMessage(player, "messages.syntax.plot.tp");
            return;
        }
        if (!args[0].matches("[0-9]")) {
            plugin.sendMessage(player, "messages.syntax.plot.tp");
            return;
        }
        if (!PlotDatabase.plotExists(Integer.parseInt(args[0]))) {
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }
        Plot plot = PlotDatabase.getPlot(Integer.parseInt(args[0]));
        assert plot != null;
        if (CityDatabase.getCity(plot.getCityID()).isEmpty()) {
            plugin.sendMessage(player, "messages.error.city.missing.city");
            return;
        }
        City city = CityDatabase.getCity(plot.getCityID()).get();
        plugin.sendMessage(player, "messages.city.successful.set.plot.tp", "%plotname%", plot.getPlotName(), "%cityname%", city.getCityName());
        player.teleport(plot.getPlotCenter());
    }


    @Subcommand("perm")
    @CommandCompletion("")
    public static void onPerm(Player player, String[] args) throws SQLException {
        if (!player.hasPermission("metropolis.plot.perm")) {
            plugin.sendMessage(player, "messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            player.sendMessage(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName());
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }

        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();

        if (args == null || args.length == 0) {
            if (CityDatabase.getClaim(player.getLocation()) != null) {

                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();

                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }

                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        StringBuilder stringBuilderOutsiders = new StringBuilder();
                        for (char s : plot.getPermsOutsiders()) {
                            stringBuilderOutsiders.append(s);
                        }
                        String permsOutsiders = "+" + stringBuilderOutsiders.substring(0, stringBuilderOutsiders.toString().length());
                        if (stringBuilderOutsiders.substring(0, stringBuilderOutsiders.toString().length()).isEmpty()) {
                            permsOutsiders = "§onada";
                        }
                        StringBuilder stringBuilderMembers = new StringBuilder();
                        for (char s : plot.getPermsMembers()) {
                            stringBuilderMembers.append(s);
                        }
                        String permsMembers = "+" + stringBuilderMembers.substring(0, stringBuilderMembers.toString().length());
                        if (stringBuilderMembers.substring(0, stringBuilderMembers.toString().length()).isEmpty()) {
                            permsMembers = "§onada";
                        }
                        plugin.sendMessage(player, "messages.plot.list.perm.header", "%plot%", plot.getPlotName());
                        plugin.sendMessage(player, "messages.plot.list.perm.outsiders", "%perms%", permsOutsiders);
                        plugin.sendMessage(player, "messages.plot.list.perm.members", "%perms%", permsMembers);

                        for (PlotPerms plotPerms : plot.getPlayerPlotPerms()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (char s : plotPerms.getPerms()) {
                                stringBuilder.append(s);
                            }
                            String perms = "+" + stringBuilder.substring(0, stringBuilder.toString().length());
                            if (plotPerms.getPerms().length == 0) {
                                return;
                            }
                            plugin.sendMessage(player, "messages.plot.list.perm.players", "%player%", plotPerms.getPlayerName(), "%perms%", perms);
                        }
                        plugin.sendMessage(player, "messages.plot.list.perm.permsrow.1");
                        plugin.sendMessage(player, "messages.plot.list.perm.permsrow.2");
                        plugin.sendMessage(player, "messages.plot.list.perm.permsrow.3");
                    }
                }
            }
            return;
        }
        if (args.length > 2 || args.length == 1) {
            plugin.sendMessage(player, "messages.syntax.plot.perm");
            return;
        }
        if (!args[0].equals("members") && !args[0].equals("outsiders") && !args[0].equals("-")) {
            @Deprecated
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
            if (offlinePlayer == null) {
                plugin.sendMessage(player, "messages.error.player.notFound");
                return;
            }
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }
        String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
        if (role == null) {
            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
            return;
        }
        if (role.equals("mayor") || role.equals("assistant") || role.equals("vicemayor")) {
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !role.equals("mayor")) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (args[0].equals("-")) {
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"perm\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"type\": " + "all" + ", \"from\": " + "" + ", \"to\": " + "" + ", \"player\": " + player.getUniqueId().toString() + " }");
                            plot.removePlotPerms();
                            plugin.sendMessage(player, "messages.city.successful.set.plot.perm.remove.all","%cityname%", city.getCityName());
                            return;
                        }
                        if (args[0].equals("members")) {
                            if (Utilities.parsePermChange(plot.getPermsMembers(), args[1], player, "plot") == null) {
                                plugin.sendMessage(player, "messages.error.plot.perm.notFound");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"perm\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"type\": " + "members" + ", \"from\": " + Arrays.toString(plot.getPermsMembers()) + ", \"to\": " + Utilities.parsePermChange(plot.getPermsMembers(), args[1], player, "plot") + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotPerms("members", Utilities.parsePermChange(plot.getPermsMembers(), args[1], player, "plot"), null);
                            plugin.sendMessage(player, "messages.city.successful.set.plot.perm.change.members", "%perms%", args[1], "%cityname%", city.getCityName());
                            return;
                        }

                        if (args[0].equals("outsiders")) {
                            if (Utilities.parsePermChange(plot.getPermsOutsiders(), args[1], player, "plot") == null) {
                                plugin.sendMessage(player, "messages.error.plot.perm.notFound");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"perm\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"type\": " + "outsiders" + ", \"from\": " + Arrays.toString(plot.getPermsOutsiders()) + ", \"to\": " + Utilities.parsePermChange(plot.getPermsOutsiders(), args[1], player, "plot") + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotPerms("outsiders", Utilities.parsePermChange(plot.getPermsOutsiders(), args[1], player, "plot"), null);
                            plugin.sendMessage(player, "messages.city.successful.set.plot.perm.change.outsiders", "%perms%", args[1], "%cityname%", city.getCityName());
                            return;
                        }

                        @SuppressWarnings("deprecation")
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                        if (!offlinePlayer.hasPlayedBefore()) {
                            plugin.sendMessage(player, "messages.error.player.notFound");
                            return;
                        }
                        if (plot.getPlayerPlotPerm(offlinePlayer.getUniqueId().toString()) == null || plot.getPlayerPlotPerm(offlinePlayer.getUniqueId().toString()).getPerms() == null) {
                            plot.setPlotPerms("players", Utilities.parsePerms(args[1], "plot", player), offlinePlayer.getUniqueId().toString());
                        } else {
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"perm\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"type\": " + "player" + ", \"from\": " + Arrays.toString(plot.getPlayerPlotPerm(offlinePlayer.getUniqueId().toString()).getPerms()) + ", \"to\": " + Utilities.parsePermChange(plot.getPlayerPlotPerm(offlinePlayer.getUniqueId().toString()).getPerms(), args[1], player, "plot") + ", \"issuer\": " + player.getUniqueId().toString() + ", \"player\": " + offlinePlayer.getUniqueId().toString() + " }");
                            plot.setPlotPerms("players", Utilities.parsePermChange(plot.getPlayerPlotPerm(offlinePlayer.getUniqueId().toString()).getPerms(), args[1], player, "plot"), offlinePlayer.getUniqueId().toString());
                        }
                        plugin.sendMessage(player, "messages.successful.set.plot.perm.change.player", "%player%", offlinePlayer.getName(), "%perms%", args[1], "%cityname%", city.getCityName());
                    }
                }
            }
        } else {
            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        }
    }

    @Subcommand("set")
    public static void onSet(Player player) {
        plugin.sendMessage(player, "messages.syntax.plot.set.typeAdmin");
        plugin.sendMessage(player, "messages.syntax.plot.set.set");
    }

    @Subcommand("set")
    public class Set extends BaseCommand {

        @Subcommand("owner")
        @CommandCompletion("@players")
        public static void onOwner(Player player, String playerName) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isInviter = role.equals("inviter") || role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!isInviter) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (playerName.equals("-")) {
                            if (plot.getPlotOwner() == null) {
                                plugin.sendMessage(player, "messages.error.plot.set.owner.alreadyNoOwner", "%cityname%", city.getCityName());
                                return;
                            }
                            plot.setPlotOwner(null);
                            plugin.sendMessage(player, "messages.plot.set.owner.removed", "%cityname%", city.getCityName());
                            return;
                        }
                        @SuppressWarnings("deprecation")
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                        if (!offlinePlayer.hasPlayedBefore()) {
                            plugin.sendMessage(player, "messages.error.player.notFound");
                            return;
                        }
                        if (!Arrays.stream(Objects.requireNonNull(CityDatabase.memberCityList(offlinePlayer.getUniqueId().toString()))).toList().contains(city.getCityName())) {
                            if (!plot.getPlotType().equals("vacation")) {
                                plugin.sendMessage(player, "messages.error.plot.set.owner.notInCity", "%cityname%", city.getCityName());
                                return;
                            }
                        }
                        if (plot.getPlotOwner() != null) {
                            if (plot.getPlotOwner().equals(offlinePlayer.getName())) {
                                plugin.sendMessage(player, "messages.error.plot.set.owner.alreadyOwner", "%cityname%", city.getCityName());
                                return;
                            }
                        }
                        plot.setPlotOwner(offlinePlayer.getName());
                        plot.setPlotOwnerUUID(offlinePlayer.getUniqueId().toString());
                        plugin.sendMessage(player, "messages.plot.set.owner.success", "%player%", offlinePlayer.getName(), "%cityname%", city.getCityName());
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("type")
        @CommandCompletion("@plotType")
        public static void onType(Player player, String type) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isAssistant = role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (type.equals("-")) {
                            if (!isAssistant) {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                                return;
                            }
                            if (plot.getPlotType() == null) {
                                plugin.sendMessage(player, "messages.error.plot.set.type.alreadyNoType", "%cityname%", city.getCityName());
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + null + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotType(null);
                            plugin.sendMessage(player, "messages.plot.set.type.removed", "%cityname%", city.getCityName());
                            return;
                        }
                        if (type.equals("church")) {
                            if (!isAssistant) {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                                return;
                            }
                            if (plot.getPlotType() == null) {
                                Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + null + ", \"to\": " + "church" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotType("church");
                                plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Kyrka");
                                return;
                            }
                            if (plot.getPlotType().equals("church")) {
                                plugin.sendMessage(player, "messages.error.plot.set.type.alreadyType", "%cityname%", city.getCityName(), "%type%", "Kyrka");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + "church" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotType("church");
                            plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Kyrka");
                            return;
                        }
                        if (type.equals("farm")) {
                            if (!isAssistant) {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                                return;
                            }
                            if (plot.getPlotType() == null) {
                                Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + null + ", \"to\": " + "farm" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotType("farm");
                                plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Farm");
                                return;
                            }
                            if (plot.getPlotType().equals("farm")) {
                                plugin.sendMessage(player, "messages.error.plot.set.type.alreadyType", "%cityname%", city.getCityName(), "%type%", "Farm");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + "farm" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotType("farm");
                            plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Farm");
                            return;
                        }
                        if (type.equals("shop")) {
                            if (!isAssistant) {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                                return;
                            }
                            if (plot.getPlotType() == null) {
                                Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + null + ", \"to\": " + "shop" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotType("shop");
                                plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Affär");
                                return;
                            }
                            if (plot.getPlotType().equals("shop")) {
                                plugin.sendMessage(player, "messages.error.plot.set.type.alreadyType", "%cityname%", city.getCityName(), "%type%", "Affär");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + "shop" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotType("shop");
                            plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Affär");
                            return;
                        }
                        if (type.equals("vacation")) {
                            if (!isAssistant) {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                                return;
                            }
                            if (plot.getPlotType() == null) {
                                Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + null + ", \"to\": " + "vacation" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotType("vacation");
                                plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Ferietomt");
                                return;
                            }
                            if (plot.getPlotType().equals("vacation")) {
                                plugin.sendMessage(player, "messages.error.plot.set.type.alreadyType", "%cityname%", city.getCityName(), "%type%", "Ferietomt");
                                return;
                            }
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + "vacation" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotType("vacation");
                            plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Ferietomt");
                            return;
                        }
                        if (type.equals("jail")) {
                            if (player.hasPermission("metropolis.admin")) {
                                if (plot.getPlotType() == null) {
                                    Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + null + ", \"to\": " + "jail" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                    plot.setPlotType("jail");
                                    plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Fängelse");
                                    return;
                                }
                                if (plot.getPlotType().equals("jail")) {
                                    plugin.sendMessage(player, "messages.error.plot.set.type.alreadyType", "%cityname%", city.getCityName(), "%type%", "Fängelse");
                                    return;
                                }
                                Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"type\", \"id\": " + plot.getPlotID() + ", \"name\": " + plot.getPlotName() + ", \"from\": " + plot.getPlotType() + ", \"to\": " + "jail" + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                                plot.setPlotType("jail");
                                plugin.sendMessage(player, "messages.plot.set.type.success", "%cityname%", city.getCityName(), "%type%", "Fängelse");
                            } else {
                                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            }
                            return;
                        }
                        plugin.sendMessage(player, "messages.error.plot.set.type.invalidType", "%cityname%", city.getCityName());
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            } else {
                plugin.sendMessage(player, "messages.error.plot.notFound");
            }
        }

        @Subcommand("name")
        public static void onName(Player player, String name) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isInviter = role.equals("inviter") || role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (Objects.equals(plot.getPlotName(), name)) {
                            plugin.sendMessage(player, "messages.error.plot.set.name.alreadyName", "%cityname%", city.getCityName(), "%plotname%", name);
                            return;
                        }
                        if (!isInviter) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (name.equals("-")) {
                            Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"name\", \"id\": " + plot.getPlotID() + ", \"from\": " + plot.getPlotName() + ", \"to\": " + "Tomt #" + plot.getPlotID() + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                            plot.setPlotName("Tomt #" + plot.getPlotID());
                            plugin.sendMessage(player, "messages.plot.set.name.success", "%cityname%", city.getCityName(), "%plotname%", name);
                            return;
                        }
                        if (name.length() > 16) {
                            plugin.sendMessage(player, "messages.error.plot.set.name.tooLong", "%cityname%", city.getCityName());
                            return;
                        }
                        Database.addLogEntry(city,"{ \"type\": \"plot\", \"subtype\": \"name\", \"id\": " + plot.getPlotID() + ", \"from\": " + plot.getPlotName() + ", \"to\": " + name + ", \"issuer\": " + player.getUniqueId().toString() + " }");
                        plot.setPlotName(name);
                        plugin.sendMessage(player, "messages.plot.set.name.success", "%cityname%", city.getCityName(), "%plotname%", name);
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("rent")
        public static void onRent(Player player, String rent) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isViceMayor = role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!isViceMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (rent.equals("-") || rent.equals("0")) {
                            plot.setPlotRent(0);
                            plugin.sendMessage(player, "messages.plot.set.rent.removed", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!rent.matches("[0-9]")) {
                            plugin.sendMessage(player, "messages.syntax.plot.set.rent");
                            return;
                        }
                        int rentInt = Integer.parseInt(rent);
                        if (rentInt > 100000) {
                            plugin.sendMessage(player, "messages.error.plot.set.rent.tooHigh", "%cityname%", city.getCityName());
                            return;
                        }
                        player.sendMessage(rent);
                        plot.setPlotRent(rentInt);
                        plugin.sendMessage(player, "messages.plot.set.rent.success", "%cityname%", city.getCityName(), "%rent%", rent);
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }
    }

    @Subcommand("toggle")
    public static void onToggle(Player player) {
        if (player.hasPermission("metropolis.admin.plot.toggle.keepinv")) {
            player.sendMessage("§7Syntax: /plot toggle §nkeepinv");
        }
        if (player.hasPermission("metropolis.admin.plot.toggle.keepexp")) {
            player.sendMessage("§7Syntax: /plot toggle §nkeepexp");
        }
        if (player.hasPermission("metropolis.admin.plot.toggle.pvp")) {
            player.sendMessage("§7Syntax: /plot toggle §npvp");
        }
        if (player.hasPermission("metropolis.admin.plot.toggle.lock")) {
            player.sendMessage("§7Syntax: /plot toggle §nlock");
        }
        plugin.sendMessage(player, "messages.syntax.plot.toggle");
    }

    @Subcommand("toggle")
    public class Toggle extends BaseCommand {

        @Subcommand("pvp")
        public static void onPvp(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            if (!player.hasPermission("metropolis.admin.plot.toggle.pvp")) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.hasFlag('p')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-p")));
                            plugin.sendMessage(player, "messages.plot.toggle.pvp.success", "%cityname%", city.getCityName(), "%status%", "inaktiverat");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("p");
                            plugin.sendMessage(player, "messages.plot.toggle.pvp.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+p")));
                        plugin.sendMessage(player, "messages.plot.toggle.pvp.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("animals")
        public static void onAnimals(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isAssistant = role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!isAssistant) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (plot.hasFlag('a')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-a")));
                            plugin.sendMessage(player, "messages.plot.toggle.animals.success", "%cityname%", city.getCityName(), "%status%", "inaktiverat");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("a");
                            plugin.sendMessage(player, "messages.plot.toggle.animals.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+a")));
                        plugin.sendMessage(player, "messages.plot.toggle.animals.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("mobs")
        public static void onMobs(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isAssistant = role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!isAssistant) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (plot.hasFlag('m')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-m")));
                            plugin.sendMessage(player, "messages.plot.toggle.monsters.success", "%cityname%", city.getCityName(), "%status%", "inaktiverat");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("m");
                            plugin.sendMessage(player, "messages.plot.toggle.monsters.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+m")));
                        plugin.sendMessage(player, "messages.plot.toggle.monsters.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("meeting")
        public static void onMeeting(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isAssistant = role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.isKMarked() && !isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (!isAssistant) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (plot.hasFlag('c')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-c")));
                            plugin.sendMessage(player, "messages.plot.toggle.meeting.success", "%cityname%", city.getCityName(), "%status%", "inaktiverat");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("c");
                            plugin.sendMessage(player, "messages.plot.toggle.meeting.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+c")));
                        plugin.sendMessage(player, "messages.plot.toggle.meeting.success", "%cityname%", city.getCityName(), "%status%", "aktiverat");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("keepexp")
        public static void onXp(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            if (!player.hasPermission("metropolis.admin.plot.toggle.keepexp")) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.hasFlag('x')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-x")));
                            plugin.sendMessage(player, "messages.plot.toggle.keepexp.success", "%cityname%", city.getCityName(), "%status%", "tappas");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("x");
                            plugin.sendMessage(player, "messages.plot.toggle.keepexp.success", "%cityname%", city.getCityName(), "%status%", "behålls");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+x")));
                        plugin.sendMessage(player, "messages.plot.toggle.keepexp.success", "%cityname%", city.getCityName(), "%status%", "behålls");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("keepinv")
        public static void onKeepInv(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            if (!player.hasPermission("metropolis.admin.plot.toggle.keepinv")) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.hasFlag('i')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-i")));
                            plugin.sendMessage(player, "messages.plot.toggle.keepinv.success", "%cityname%", city.getCityName(), "%status%", "tappas");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("i");
                            plugin.sendMessage(player, "messages.plot.toggle.keepinv.success", "%cityname%", city.getCityName(), "%status%", "behålls");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+i")));
                        plugin.sendMessage(player, "messages.plot.toggle.keepinv.success", "%cityname%", city.getCityName(), "%status%", "behålls");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("lock")
        public static void onLock(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            if (!player.hasPermission("metropolis.admin.plot.toggle.lock")) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (plot.hasFlag('l')) {
                            plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "-l")));
                            plugin.sendMessage(player, "messages.plot.toggle.locked.success", "%cityname%", city.getCityName(), "%status%", "olåst");
                            return;
                        }
                        if (plot.getPlotFlags().length == 0) {
                            plot.setPlotFlags("l");
                            plugin.sendMessage(player, "messages.plot.toggle.locked.success", "%cityname%", city.getCityName(), "%status%", "låst");
                            return;
                        }
                        plot.setPlotFlags(Objects.requireNonNull(Utilities.parseFlagChange(plot.getPlotFlags(), "+l")));
                        plugin.sendMessage(player, "messages.plot.toggle.locked.success", "%cityname%", city.getCityName(), "%status%", "låst");
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }

        @Subcommand("k")
        public static void onKMark(Player player) {
            if (CityDatabase.getClaim(player.getLocation()) == null) {
                plugin.sendMessage(player, "messages.error.plot.notFound");
                return;
            }
            City city = CityDatabase.getCityByClaim(player.getLocation());
            assert city != null;
            String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
            if (role == null) {
                plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                return;
            }
            boolean isMayor = role.equals("mayor");
            if (CityDatabase.getClaim(player.getLocation()) != null) {
                for (Plot plot : city.getCityPlots()) {
                    Polygon polygon = new Polygon();
                    for (Location loc : plot.getPlotPoints()) {
                        polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                    }
                    if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                        if (!isMayor) {
                            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                            return;
                        }
                        if (plot.isKMarked()) {
                            plot.setKMarked(false);
                            plugin.sendMessage(player, "messages.plot.toggle.k-marked.success", "%cityname%", city.getCityName(), "%status%", "stängdes av");
                        } else {
                            plot.setKMarked(true);
                            plugin.sendMessage(player, "messages.plot.toggle.k-marked.success", "%cityname%", city.getCityName(), "%status%", "sattes på");
                        }
                        return;
                    }
                    if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                        plugin.sendMessage(player, "messages.error.plot.notFound");
                        return;
                    }
                }
            }
        }
    }

    @Subcommand("update")
    public static void onUpdate(Player player) {
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player, "messages.error.plot.notFound");
            return;
        }
        City city = CityDatabase.getCityByClaim(player.getLocation());
        assert city != null;
        String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
        if (role == null) {
            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
            return;
        }
        boolean isViceMayor = role.equals("mayor") || role.equals("vicemayor");
        if (!player.hasPermission("metropolis.admin.plot.update")) {
            plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location loc : plot.getPlotPoints()) {
                    polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (!isViceMayor) {
                        plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
                        return;
                    }
                    Polygon regionPolygon = MetropolisListener.playerPolygons.get(player.getUniqueId());
                    Location[] locations = MetropolisListener.savedLocs.get(player.getUniqueId()).toArray(new Location[0]);
                    double minX = regionPolygon.getBounds().getMinX();
                    double maxX = regionPolygon.getBounds().getMaxX();
                    double minY = regionPolygon.getBounds().getMinY();
                    double maxY = regionPolygon.getBounds().getMaxY();

                    if (maxX - minX < 3 || maxY - minY < 3) {
                        plugin.sendMessage(player, "messages.error.plot.tooSmall");
                        return;
                    }
                    if (MetropolisListener.playerYMax.get(player.getUniqueId()) - MetropolisListener.playerYMin.get(player.getUniqueId()) < 3) {
                        plugin.sendMessage(player, "messages.error.plot.tooLowY");
                        return;
                    }

                    int chunkSize = 16;
                    int startX = (int) Math.floor(minX / chunkSize) * chunkSize;
                    int endX = (int) Math.floor(maxX / chunkSize) * chunkSize + chunkSize;
                    int startY = (int) Math.floor(minY / chunkSize) * chunkSize;
                    int endY = (int) Math.floor(maxY / chunkSize) * chunkSize + chunkSize;

                    Rectangle chunkBounds = new Rectangle();
                    for (int x = startX; x < endX; x += chunkSize) {
                        for (int z = startY; z < endY; z += chunkSize) {
                            chunkBounds.setBounds(x, z, chunkSize, chunkSize);
                            if (regionPolygon.intersects(chunkBounds)) {
                                if (CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z)) == null || !Objects.equals(Objects.requireNonNull(CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z))).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                                    plugin.sendMessage(player, "messages.error.plot.intersectsExistingClaim");
                                    return;
                                }
                                if (PlotDatabase.intersectsExistingPlot(regionPolygon, city)) {
                                    plugin.sendMessage(player, "messages.error.plot.intersectsExistingPlot");
                                    return;
                                }
                                plot.updatePlot(player, locations, MetropolisListener.playerYMin.get(player.getUniqueId()), MetropolisListener.playerYMax.get(player.getUniqueId()));
                                MetropolisListener.savedLocs.remove(player.getUniqueId());
                                MetropolisListener.playerPolygons.remove(player.getUniqueId());
                                MetropolisListener.playerYMin.remove(player.getUniqueId());
                                MetropolisListener.playerYMax.remove(player.getUniqueId());
                                plugin.sendMessage(player, "messages.city.successful.set.plot.new", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                                plugin.sendMessage(player, "messages.plot.update.success", "%cityname%", city.getCityName());
                                return;
                            }
                            if (city.getCityPlots().indexOf(plot) == city.getCityPlots().size() - 1) {
                                plugin.sendMessage(player, "messages.error.plot.notFound");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
