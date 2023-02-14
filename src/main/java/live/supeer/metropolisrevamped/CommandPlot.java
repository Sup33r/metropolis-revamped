package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import live.supeer.metropolisrevamped.plot.Plot;
import live.supeer.metropolisrevamped.plot.PlotDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandAlias("plot")
public class CommandPlot extends BaseCommand  {
    static MetropolisRevamped plugin;

    @Default
    public static void onPlot(Player player) {

    }

    @Subcommand("new")
    public static void onNew(Player player, String[] args) {
        if (!player.hasPermission("metropolis.plot.new")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (!MetropolisListener.playerPolygons.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player,"messages.error.missing.plot");
            return;
        }
        if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).isEmpty()) {
            plugin.sendMessage(player,"messages.error.missing.city");
        }
        City city = CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString())).get();
        if (Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "member") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "inviter")) {
            plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
            return;
        }
        Polygon regionPolygon = MetropolisListener.playerPolygons.get(player.getUniqueId());
        Location[] locations = MetropolisListener.savedLocs.get(player.getUniqueId()).toArray(new Location[0]);
        double minX = regionPolygon.getBounds().getMinX();
        double maxX = regionPolygon.getBounds().getMaxX();
        double minY = regionPolygon.getBounds().getMinY();
        double maxY = regionPolygon.getBounds().getMaxY();

        if (maxX - minX < 3 || maxY - minY < 3) {
            plugin.sendMessage(player,"messages.error.plot.tooSmall");
            return;
        }
        if (MetropolisListener.playerYMax.get(player.getUniqueId()) - MetropolisListener.playerYMin.get(player.getUniqueId()) < 3) {
            plugin.sendMessage(player,"messages.error.plot.tooLowY");
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
                    if (CityDatabase.getClaim(new Location(player.getWorld(),x,0,z)) == null || !Objects.equals(Objects.requireNonNull(CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z))).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                        plugin.sendMessage(player,"messages.error.plot.intersectsExistingClaim");
                        return;
                    }
                    if (PlotDatabase.intersectsExistingPlot(regionPolygon,city)) {
                        plugin.sendMessage(player,"messages.error.plot.intersectsExistingPlot");
                        return;
                    }
                    Plot plot = PlotDatabase.createPlot(player,locations,null,city,MetropolisListener.playerYMin.get(player.getUniqueId()),MetropolisListener.playerYMax.get(player.getUniqueId()));
                    assert plot != null;
                    plugin.sendMessage(player,"messages.city.successful.set.plot.new","%cityname%",city.getCityName(),"%plotname%",plot.getPlotName());
                }
            }
        }

    }

    @Subcommand("expand")
    public static void onExpand(Player player, String[] args) {
        if (!player.hasPermission("metropolis.plot.expand")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (!MetropolisListener.playerPolygons.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player,"messages.error.missing.plot");
            return;
        }

        if (args.length == 0) {
            MetropolisListener.playerYMin.put(player.getUniqueId(),-64);
            MetropolisListener.playerYMax.put(player.getUniqueId(),319);
            plugin.sendMessage(player,"messages.city.successful.set.plot.expand.max");
            return;
        }
        if (args.length > 2) {
            plugin.sendMessage(player,"messages.syntax.plot.expand");
            return;
        }
        if (args.length == 1) {
            if (args[0].equals("up")) {
                MetropolisListener.playerYMax.put(player.getUniqueId(),319);
                plugin.sendMessage(player,"messages.city.successful.set.plot.expand.up.max");
                return;
            } else if (args[0].equals("down")) {
                MetropolisListener.playerYMin.put(player.getUniqueId(),-64);
                plugin.sendMessage(player,"messages.city.successful.set.plot.expand.down.max");
                return;
            } else {
                plugin.sendMessage(player,"messages.syntax.plot.expand");
                return;
            }
        }
        if (args[0].matches("[0-9]")) {
            if (args[1].equals("up")) {
                if (MetropolisListener.playerYMax.get(player.getUniqueId()) + Integer.parseInt(args[0]) > 319) {
                    plugin.sendMessage(player,"messages.error.plot.tooLowExpand");
                    return;
                }
                MetropolisListener.playerYMax.put(player.getUniqueId(),MetropolisListener.playerYMax.get(player.getUniqueId()) + Integer.parseInt(args[0]));
                plugin.sendMessage(player,"messages.city.successful.set.plot.expand.up","%y%",args[0]);
            } else if (args[1].equals("down")) {
                if (MetropolisListener.playerYMin.get(player.getUniqueId()) - Integer.parseInt(args[0]) < -64) {
                    plugin.sendMessage(player,"messages.error.plot.tooHighExpand");
                    return;
                }
                MetropolisListener.playerYMin.put(player.getUniqueId(),MetropolisListener.playerYMin.get(player.getUniqueId()) - Integer.parseInt(args[0]));
                plugin.sendMessage(player,"messages.city.successful.set.plot.expand.down","%y%",args[0]);
            } else {
                plugin.sendMessage(player,"messages.syntax.plot.expand");
            }
        } else {
            plugin.sendMessage(player,"messages.syntax.plot.expand");
        }
    }

    @Subcommand("delete")
    public static void onDelete(Player player) {

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
                        plugin.sendMessage(player, "messages.city.successful.set.plot.leave", "%cityname%", city.getCityName(), "%plotname%", plot.getPlotName());
                    } else {
                        plugin.sendMessage(player, "messages.error.plot.notOwner");
                    }
                    return;
                }
            }
        }
    }

    @Subcommand("market")
    public static void onMarket(Player player, String arg) {
        if (!player.hasPermission("metropolis.plot.market")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player,"messages.error.plot.notInPlot");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            plugin.sendMessage(player,"messages.error.plot.notInPlot");
            return;
        }
        City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String role = CityDatabase.getCityRole(city,player.getUniqueId().toString());
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location location : plot.getPlotPoints()) {
                    polygon.addPoint(location.getBlockX(), location.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (Objects.equals(plot.getPlotOwnerUUID(), player.getUniqueId().toString()) || Objects.equals(role, "inviter") || Objects.equals(role, "assistant") || Objects.equals(role, "vicemayor") || Objects.equals(role, "mayor")) {
                        if (arg.equals("-")) {
                            plot.setForSale(false);
                            plugin.sendMessage(player,"messages.city.successful.set.plot.market.remove","%cityname%",city.getCityName(),"%plotname%",plot.getPlotName());
                            return;
                        }
                        if (arg.matches("[0-9]")) {
                            plot.setForSale(true);
                            plot.setPlotPrice(Integer.parseInt(arg));
                            plugin.sendMessage(player,"messages.city.successful.set.plot.market.set","%cityname%",city.getCityName(),"%plotname%",plot.getPlotName(),"%amount%",arg);
                            return;
                        }
                        plugin.sendMessage(player,"messages.syntax.plot.market");
                    } else {
                        plugin.sendMessage(player,"messages.error.city.permissionDenied","%cityname%",city.getCityName());
                    }
                    return;
                }
            }
            plugin.sendMessage(player,"messages.error.plot.notInPlot");
        } else {
            plugin.sendMessage(player,"messages.error.plot.notInPlot");
        }
    }

    @Subcommand("info")
    public static void onInfo(Player player) {
        if (!player.hasPermission("metropolis.plot.info")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (CityDatabase.getClaim(player.getLocation()) == null) {
            plugin.sendMessage(player,"messages.error.plot.notFound");
            return;
        }
        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
            player.sendMessage(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName());
            plugin.sendMessage(player,"messages.error.plot.notFound");
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
                    plugin.sendMessage(player,"messages.plot.list.header","%plot%",plot.getPlotName());
                    plugin.sendMessage(player,"messages.plot.list.id","%id%", String.valueOf(plot.getPlotID()));
                    plugin.sendMessage(player,"messages.plot.list.city","%cityname%",city.getCityName());
                    plugin.sendMessage(player,"messages.plot.list.owner","%owner%",plot.getPlotOwner());
                    plugin.sendMessage(player,"messages.plot.list.pvp","%status%","placeholder");
                    plugin.sendMessage(player,"messages.plot.list.animals","%status%","placeholder");
                    plugin.sendMessage(player,"messages.plot.list.monsters","%status%","placeholder");
                    plugin.sendMessage(player,"messages.plot.list.locked","%status%","placeholder");
                    if (plot.isKMarked()) {
                        plugin.sendMessage(player, "messages.plot.list.k-marked", "%status%", "§cJa");
                    } else {
                        plugin.sendMessage(player, "messages.plot.list.k-marked", "%status%", "§aNej");
                    }
                    plugin.sendMessage(player,"messages.plot.list.lose.items","%status%", "placeholder");
                    plugin.sendMessage(player,"messages.plot.list.lose.xp","%status%", "placeholder");
                    if (player.hasPermission("metropolis.plot.info.coordinates") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "assistant") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "vicemayor") || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "mayor")) {
                        plugin.sendMessage(player,"messages.plot.list.middle","%world%",plot.getPlotCenter().getWorld().getName(),"%x%", String.valueOf(plot.getPlotCenter().getBlockX()),"%y%", String.valueOf(plot.getPlotCenter().getBlockY()),"%z%", String.valueOf(plot.getPlotCenter().getBlockZ()));
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
                        plugin.sendMessage(player,"messages.plot.list.players","%players%",stringBuilder.substring(0, stringBuilder.toString().length() - 2));
                    }
                }
            }
        } else {
            plugin.sendMessage(player,"messages.error.plot.notFound");
        }
    }

    @Subcommand("player")
    public static void onPlayer(Player player, String playerName) {
        if (!player.hasPermission("metropolis.plot.player")) {
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        @Deprecated
        Player p = Bukkit.getOfflinePlayer(playerName).getPlayer();
        if (p == null) {
            plugin.sendMessage(player,"messages.error.player.notFound");
            return;
        }
        if (CityDatabase.memberCityList(p.getUniqueId().toString()) == null || Objects.requireNonNull(CityDatabase.memberCityList(p.getUniqueId().toString())).length == 0) {
            plugin.sendMessage(player,"messages.error.city.notInCity");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String c : Objects.requireNonNull(CityDatabase.memberCityList(p.getUniqueId().toString()))) {
            if (CityDatabase.getCity(c).isEmpty()) {return;}
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
            plugin.sendMessage(player,"messages.error.permissionDenied");
            return;
        }
        if (args.length != 1) {
            plugin.sendMessage(player,"messages.syntax.plot.tp");
            return;
        }
        if (!args[0].matches("[0-9]")) {
            plugin.sendMessage(player,"messages.syntax.plot.tp");
            return;
        }
        if (!PlotDatabase.plotExists(Integer.parseInt(args[0]))) {
            plugin.sendMessage(player,"messages.error.plot.notFound");
            return;
        }
        Plot plot = PlotDatabase.getPlot(Integer.parseInt(args[0]));
        assert plot != null;
        if (CityDatabase.getCity(plot.getCityID()).isEmpty()) {
            plugin.sendMessage(player,"messages.error.city.missing.city");
            return;
        }
        City city = CityDatabase.getCity(plot.getCityID()).get();
        plugin.sendMessage(player,"messages.city.successful.set.plot.tp","%plotname%",plot.getPlotName(),"%cityname%",city.getCityName());
        player.teleport(plot.getPlotCenter());
    }


}
