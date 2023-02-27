package live.supeer.metropolisrevamped;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.plot.Plot;
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
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {Utilities.sendNatureScoreboard(player);}
            City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
            playerInCity.put(player.getUniqueId(),city);
            Utilities.sendCityScoreboard(player, city);
        } else {
            Utilities.sendNatureScoreboard(player);
        }
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

    public static HashMap<UUID, Polygon> playerPolygons = new HashMap<>();
    public static HashMap<UUID, Integer> playerYMin = new HashMap<>();
    public static HashMap<UUID, Integer> playerYMax = new HashMap<>();
    public static HashMap<UUID, City> playerInCity = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (playerInCity.containsKey(event.getPlayer().getUniqueId()) && CityDatabase.getClaim(to) == null) {
            Utilities.sendNatureScoreboard(event.getPlayer());
            playerInCity.remove(event.getPlayer().getUniqueId());
        }
        if (playerInCity.containsKey(event.getPlayer().getUniqueId())) {
            if (CityDatabase.getClaim(to) != null) {
                if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(to)).getCityName()).isEmpty()) {
                    Utilities.sendNatureScoreboard(event.getPlayer());
                    playerInCity.remove(event.getPlayer().getUniqueId());
                    return;
                }
                City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(to)).getCityName()).get();
                if (!city.getCityName().equals(playerInCity.get(event.getPlayer().getUniqueId()).getCityName())) {
                    Utilities.sendCityScoreboard(event.getPlayer(), city);
                    playerInCity.remove(event.getPlayer().getUniqueId());
                    playerInCity.put(event.getPlayer().getUniqueId(),city);
                }
                Utilities.sendCityScoreboard(event.getPlayer(), city);
            }
        }

        if ((from.getBlockX() >> 4) != (to.getBlockX() >> 4) || (from.getBlockZ() >> 4) != (to.getBlockZ() >> 4)) {
            if (CityDatabase.getClaim(to) != null) {
                if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(to)).getCityName()).isEmpty()) {Utilities.sendNatureScoreboard(event.getPlayer());}
                City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(to)).getCityName()).get();
                if (playerInCity.containsKey(event.getPlayer().getUniqueId())) {
                    if (!city.getCityName().equals(playerInCity.get(event.getPlayer().getUniqueId()).getCityName())) {
                        if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(from)).getCityName()).isPresent()) {
                            City fromCity = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(from)).getCityName()).get();
                            if (fromCity.getExitMessage() != null) {
                                plugin.sendMessage(event.getPlayer(),"messages.city.exit","%cityname%",fromCity.getCityName(), "%exit%", fromCity.getExitMessage());
                            }
                        }
                        if (city.getEnterMessage() != null) {
                            plugin.sendMessage(event.getPlayer(),"messages.city.enter","%cityname%",city.getCityName(), "%enter%", city.getEnterMessage());
                        }
                    }
                }
                Utilities.sendCityScoreboard(event.getPlayer(), city);
                playerInCity.remove(event.getPlayer().getUniqueId());
                playerInCity.put(event.getPlayer().getUniqueId(),city);
            } else {
                if (playerInCity.containsKey(event.getPlayer().getUniqueId())) {
                    if (CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(from)).getCityName()).isPresent()) {
                        City fromCity = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(from)).getCityName()).get();
                        if (fromCity.getExitMessage() != null) {
                            plugin.sendMessage(event.getPlayer(),"messages.city.exit","%cityname%",fromCity.getCityName(), "%exit%", fromCity.getExitMessage());
                        }
                    }
                }
                Utilities.sendNatureScoreboard(event.getPlayer());
                playerInCity.remove(event.getPlayer().getUniqueId());
            }
        }
    }


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
                if (CommandCity.blockEnabled.contains(player)) {
                    if (CityDatabase.getClaim(player.getLocation()) == null || CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).isEmpty()) {
                        plugin.sendMessage(player, "messages.error.permissionDenied");
                        return;
                    }
                    City city = CityDatabase.getCity(Objects.requireNonNull(CityDatabase.getClaim(player.getLocation())).getCityName()).get();
                    String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
                    assert role != null;
                    for (Plot plot : city.getCityPlots()) {
                        Polygon polygon = new Polygon();
                        int ymin = plot.getPlotYMin();
                        int ymax = plot.getPlotYMax();
                        for (Location location : plot.getPlotPoints()) {
                            polygon.addPoint(location.getBlockX(), location.getBlockZ());
                        }
                        if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ()) && player.getLocation().getBlockY() >= ymin && player.getLocation().getBlockY() <= ymax) {
                            if (Objects.equals(plot.getPlotOwnerUUID(), player.getUniqueId().toString()) || Objects.equals(role, "assistant") || Objects.equals(role, "vicemayor") || Objects.equals(role, "mayor")) {
                            }
                        }
                    }
                    boolean isViceMayor = role.equals("mayor") || role.equals("vicemayor");
                    return;
                }
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
