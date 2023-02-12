package live.supeer.metropolisrevamped;

import fr.mrmicky.fastboard.FastBoard;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.city.Claim;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MetropolisListener implements Listener {
    static MetropolisRevamped plugin;
    private static List<Location> savedLocations = new ArrayList<>();
    private static List<Player> savedPlayers = new ArrayList<>();

    private static HashMap<UUID, List<Location>> savedLocs = new HashMap<>();


    @EventHandler
    public static void playerJoinScoreboard(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);

        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String cityUn = Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName();
            if (CityDatabase.getCity(cityUn).isEmpty()) {
                return;
            }
            City city = CityDatabase.getCity(cityUn).get();
            board.updateLines(
                    "",
                    plugin.getMessage("messages.city.scoreboard.members"),
                    "§a" + city.getCityMembers().size(),
                    "",
                    plugin.getMessage("messages.city.scoreboard.plots"),
                    "§a" + city.getCityClaims().size()
            );
        } else {
            board.updateTitle(plugin.getMessage("messages.city.scoreboard.nature"));
            board.updateLine(0,plugin.getMessage("messages.city.scoreboard.pvp_on"));
        }
    }

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String[] list = CityDatabase.memberCityList(player.getUniqueId().toString());
        for (int i = 0; i < Objects.requireNonNull(list).length; i++) {
            if (CityDatabase.getCity(list[i]).isPresent()) {
                City city = CityDatabase.getCity(list[i]).get();
                String cityMotd = city.getMotdMessage();
                if (cityMotd != null) {
                    plugin.sendMessage(player,"messages.city.motd","%cityname%",city.getCityName(),"%motd%",cityMotd);
                }
            }
        }
    }

    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        //Lägga in en if-sats som kollar om spelaren är i en stad eller inte, och därefter endast kolla efter tomter om de är i en stad.
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            if (CityDatabase.getCity(CityDatabase.getClaim(player.getLocation()).getCityName()).isEmpty()) {
                return;
            }
            City city = CityDatabase.getCity(CityDatabase.getClaim(player.getLocation()).getCityName()).get();
            board.updateTitle("§a§l" + city.getCityName());
            board.updateLine(5,"§a" + city.getCityClaims().size());
            board.updateLine(4,plugin.getMessage("messages.city.scoreboard.plots"));
            board.updateLine(3," ");
            board.updateLine(2,"§a" + city.getCityMembers().size());
            board.updateLine(1,plugin.getMessage("messages.city.scoreboard.members"));
            board.updateLine(0," ");
        } else {
            board.updateTitle(plugin.getMessage("messages.city.scoreboard.nature"));
            board.updateLine(0,plugin.getMessage("messages.city.scoreboard.pvp_on"));
        }
    }

    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STICK) {
                event.setCancelled(true);
                savedPlayers.remove(player);
                savedLocations.clear();
                savedLocations.add(event.getClickedBlock().getLocation());
                plugin.sendMessage(player,"messages.city.markings.new", "%world%", event.getClickedBlock().getWorld().getName(), "%x%", String.valueOf(event.getClickedBlock().getX()), "%y%", String.valueOf(event.getClickedBlock().getY()), "%z%", String.valueOf(event.getClickedBlock().getZ()));
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STICK) {
                event.setCancelled(true);
                if (savedPlayers.contains(player)) {
                    player.sendMessage("§cDu har redan en markering igång.");
                    return;
                }
                if (savedLocations.get(savedLocations.size()-1) == event.getClickedBlock().getLocation()) {
                    player.sendMessage("§cDu kan inte markera samma block två gånger.");
                    return;
                }
                if (savedLocations.get(savedLocations.size()-1).getWorld() != event.getClickedBlock().getWorld()) {
                    player.sendMessage("§cDu kan inte markera block i olika världar.");
                    return;
                }
                if (savedLocations.get(savedLocations.size()-1).distance(event.getClickedBlock().getLocation()) > 1) {
                    player.sendMessage("§cDu måste markera blocken i en rät linje.");
                    return;
                }
                savedLocations.add(event.getClickedBlock().getLocation());
                plugin.sendMessage(player,"messages.city.markings.add", "%world%", event.getClickedBlock().getWorld().getName(), "%x%", String.valueOf(event.getClickedBlock().getX()), "%y%", String.valueOf(event.getClickedBlock().getY()), "%z%", String.valueOf(event.getClickedBlock().getZ()), "%number%", String.valueOf(savedLocations.size()));
                if (savedLocations.size() > 2 && savedLocations.get(0).equals(savedLocations.get(savedLocations.size() - 1))) {
                    Polygon polygon = new Polygon();
                    for (Location location : savedLocations) {
                        polygon.addPoint(location.getBlockX(), location.getBlockZ());
                    }
                    double minX = polygon.getBounds().getMinX();
                    double maxX = polygon.getBounds().getMaxX();
                    double minY = polygon.getBounds().getMinY();
                    double maxY = polygon.getBounds().getMaxY();

                    player.sendMessage("§aMinX: " + minX + " MaxX: " + maxX + " MinY: " + minY + " MaxY: " + maxY);
                    int chunkSize = 16;
                    int startX = (int) Math.floor(minX / chunkSize) * chunkSize;
                    int endX = (int) Math.floor(maxX / chunkSize) * chunkSize + chunkSize;
                    int startY = (int) Math.floor(minY / chunkSize) * chunkSize;
                    int endY = (int) Math.floor(maxY / chunkSize) * chunkSize + chunkSize;

                    Rectangle chunkBounds = new Rectangle();
                    for (int x = startX; x < endX; x += chunkSize) {
                        for (int z = startY; z < endY; z += chunkSize) {
                            chunkBounds.setBounds(x, z, chunkSize, chunkSize);
                            if (polygon.intersects(chunkBounds)) {
                                if (CityDatabase.getClaim(new Location(player.getWorld(),x,0,z)) == null || !Objects.equals(CityDatabase.getClaim(new Location(player.getWorld(), x, 0, z)).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                                    player.sendMessage("§cEn markbit inom din markering är inte i din stad.");
                                    return;
                                }
                                player.sendMessage("§aX: " + x + " Z: " + z);
                            }
                        }
                    }

                    plugin.sendMessage(player,"messages.city.markings.finish");
                    savedPlayers.add(player);
                }
            }
        }
    }

}
