package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

public class CommandCity extends BaseCommand {

    @Subcommand("info")
    @Default
    public static void onInfo(Player player, @Optional String cityName) {

    }
}
