package live.supeer.metropolisrevamped;

import fr.mrmicky.fastboard.FastBoard;
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
            String city = CityDatabase.getClaim(player.getLocation());
            board.updateLines(
                    "",
                    plugin.getMessage("message.city.scoreboard.members"),
                    "§a" + CityDatabase.getCityMemberCount(city),
                    "",
                    plugin.getMessage("message.city.scoreboard.plots"),
                    "§a" + CityDatabase.getCityMemberCount(city)
            );
        } else {
            board.updateTitle(plugin.getMessage("message.city.scoreboard.nature"));
            board.updateLine(0,plugin.getMessage("message.city.scoreboard.pvp_on"));
        }
    }
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        //Lägga in en if-sats som kollar om spelaren är i en stad eller inte, och därefter endast kolla efter tomter om de är i en stad.
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String city = CityDatabase.getClaim(player.getLocation());
            board.updateTitle("§a§l" + city);
            board.updateLine(5,"§a" + CityDatabase.getCityMemberCount(city));
            board.updateLine(4,plugin.getMessage("message.city.scoreboard.plots"));
            board.updateLine(3," ");
            board.updateLine(2,"§a" + CityDatabase.getCityMemberCount(city));
            board.updateLine(1,plugin.getMessage("message.city.scoreboard.members"));
            board.updateLine(0," ");
        } else {
            board.updateTitle(plugin.getMessage("message.city.scoreboard.nature"));
            board.updateLine(0,plugin.getMessage("message.city.scoreboard.pvp_on"));
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
