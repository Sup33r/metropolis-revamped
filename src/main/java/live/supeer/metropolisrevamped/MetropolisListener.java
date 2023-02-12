package live.supeer.metropolisrevamped;

import fr.mrmicky.fastboard.FastBoard;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
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
    private static final List<Player> savedPlayers = new ArrayList<>();

    public static HashMap<UUID, List<Location>> savedLocs = new HashMap<>();


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
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) return;
            City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
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
    public static HashMap<UUID, Polygon> playerPolygons = new HashMap<>();
    public static HashMap<UUID, Integer> playerYMin = new HashMap<>();
    public static HashMap<UUID, Integer> playerYMax = new HashMap<>();


    @EventHandler
    public static void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STICK) {
                event.setCancelled(true);
                savedPlayers.remove(player);
                savedLocs.remove(player.getUniqueId());
                savedLocs.put(player.getUniqueId(), new ArrayList<>());
                savedLocs.get(player.getUniqueId()).add(event.getClickedBlock().getLocation());
                plugin.sendMessage(player,"messages.city.markings.new", "%world%", event.getClickedBlock().getWorld().getName(), "%x%", String.valueOf(event.getClickedBlock().getX()), "%y%", String.valueOf(event.getClickedBlock().getY()), "%z%", String.valueOf(event.getClickedBlock().getZ()));
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STICK) {
                event.setCancelled(true);
                if (savedPlayers.contains(player)) {
                    plugin.sendMessage(player,"messages.city.markings.finished");
                    return;
                }
                if (savedLocs.get(player.getUniqueId()).size() > 0 && savedLocs.get(player.getUniqueId()).get(savedLocs.get(player.getUniqueId()).size()-1).equals(event.getClickedBlock().getLocation())) {
                    plugin.sendMessage(player,"messages.city.markings.sameBlock");
                    return;
                }
                if (savedLocs.get(player.getUniqueId()).size() > 0 && !savedLocs.get(player.getUniqueId()).get(0).getWorld().equals(event.getClickedBlock().getWorld())) {
                    plugin.sendMessage(player,"messages.city.markings.differentWorlds");
                    return;
                }

                savedLocs.get(player.getUniqueId()).add(event.getClickedBlock().getLocation());
                plugin.sendMessage(player,"messages.city.markings.add", "%world%", event.getClickedBlock().getWorld().getName(), "%x%", String.valueOf(event.getClickedBlock().getX()), "%y%", String.valueOf(event.getClickedBlock().getY()), "%z%", String.valueOf(event.getClickedBlock().getZ()), "%number%", String.valueOf(savedLocs.get(player.getUniqueId()).size()));
                if (savedLocs.get(player.getUniqueId()).size() > 2 && savedLocs.get(player.getUniqueId()).get(0).equals(event.getClickedBlock().getLocation())) {
                    Polygon regionPolygon = new Polygon();
                    for (Location location : savedLocs.get(player.getUniqueId())) {
                        regionPolygon.addPoint(location.getBlockX(), location.getBlockZ());
                        if (playerYMax.get(player.getUniqueId()) == null || location.getBlockY() > playerYMax.get(player.getUniqueId())) {
                            playerYMax.put(player.getUniqueId(), location.getBlockY());
                        }
                        if (playerYMin.get(player.getUniqueId()) == null || location.getBlockY() < playerYMin.get(player.getUniqueId())) {
                            playerYMin.put(player.getUniqueId(), location.getBlockY());
                        }
                    }
                    playerPolygons.put(player.getUniqueId(), regionPolygon);
                    plugin.sendMessage(player,"messages.city.markings.finish");
                    savedPlayers.add(player);
                }
            }
        }
    }

}
