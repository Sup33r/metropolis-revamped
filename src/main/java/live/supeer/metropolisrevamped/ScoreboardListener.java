package live.supeer.metropolisrevamped;

import fr.mrmicky.fastboard.FastBoard;
import live.supeer.metropolisrevamped.city.CityDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
public class ScoreboardListener implements Listener {

    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            String city = CityDatabase.getClaim(player.getLocation());
            board.updateTitle("§a§l" + city);
            board.updateLine(5,"§a" + CityDatabase.getCityMemberCount(city));
            board.updateLine(4,"§2§l" + "Antal tomter:");
            board.updateLine(3," ");
            board.updateLine(2,"§a" + CityDatabase.getCityMemberCount(city));
            board.updateLine(1,"§2§l" + "Antal medlemmar:");
            board.updateLine(0," ");
        } else {
            board.updateTitle("§2§lIcke detaljplanerad mark");
            board.updateLine(0,"§4§l" + "PVP aktiverat");
        }
    }
    @EventHandler
    public static void onPlayerMove(PlayerMoveEvent event) {

    }
}
