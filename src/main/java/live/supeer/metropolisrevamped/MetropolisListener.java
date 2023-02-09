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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MetropolisListener implements Listener {
    static MetropolisRevamped plugin;
    private static List<Vector> positions = new ArrayList<>();
    private static List<Player> savedPlayers = new ArrayList<>();

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);

        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String cityUn = CityDatabase.getClaim(player.getLocation());
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
    public static void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        //Lägga in en if-sats som kollar om spelaren är i en stad eller inte, och därefter endast kolla efter tomter om de är i en stad.
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            if (CityDatabase.getCity(CityDatabase.getClaim(player.getLocation())).isEmpty()) {
                return;
            }
            City city = CityDatabase.getCity(CityDatabase.getClaim(player.getLocation())).get();
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
                positions.clear();
                savedPlayers.remove(player);
                positions.add(event.getClickedBlock().getLocation().toVector());
                player.sendMessage("§a§lMetropolis §8» §7You have started a new polygon.");
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getMaterial() == Material.STICK) {
                event.setCancelled(true);
                if (savedPlayers.contains(player)) {
                    player.sendMessage("§a§lMetropolis §8» §7You have already saved a polygon.");
                    return;
                }
                positions.add(event.getClickedBlock().getLocation().toVector());
                player.sendMessage("§a§lMetropolis §8» §7You have added a new point to the polygon.");
                if (positions.size() > 2 && positions.get(0).equals(positions.get(positions.size() - 1))) {
                    player.sendMessage("§a§lMetropolis §8» §7You have finished the polygon.");
                    savePolygon(positions,player);

                }
            }
        }
    }
    public static void savePolygon(List<Vector> positions, Player player) {
        savedPlayers.add(player);
        for (int i = 0; i < positions.size() - 1; i++) {
            Vector pos1 = positions.get(i);
            Vector pos2 = positions.get(i + 1);
            for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()); x <= Math.max(pos1.getBlockX(), pos2.getBlockX()); x++) {
                for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()); z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()); z++) {
                    Location loc = new Location(player.getWorld(), x, 0, z);
                    loc.getBlock().setType(Material.DIAMOND_BLOCK);
                }
            }
        }
    }
}
