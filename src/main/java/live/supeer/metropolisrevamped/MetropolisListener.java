package live.supeer.metropolisrevamped;

import fr.mrmicky.fastboard.FastBoard;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class MetropolisListener implements Listener {
    static MetropolisRevamped plugin;

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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem().getType() == Material.STICK) {

            }
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.getItem().getType() == Material.STICK) {

            }
        }
    }
}
