package live.supeer.metropolisrevamped;
import fr.mrmicky.fastboard.FastBoard;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import live.supeer.metropolisrevamped.plot.Plot;
import org.bukkit.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class Utilities {
    static MetropolisRevamped plugin;
    public static String formattedMoney(Integer money) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setGroupingUsed(true);
        return formatter.format(money).replace(",", " ");
    }
    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }
    public static String locationToString(Location location) {
        if (location == null) {
            return null;
        }

        return location.getWorld().getName() + " " + location.getX() + " " + location.getY() + " " + location.getZ() + " " + location.getYaw() + " " + location.getPitch();
    }
    public static Location stringToLocation(String string) {
        if (string == null || string.length() == 0) {
            return null;
        }

        String[] split = string.split(" ");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static boolean isCloseToOtherCity(Player player, Location location) {
        int centerZ = location.getChunk().getZ();
        int centerX = location.getChunk().getX();

        for (int x = centerX - 13 / 2; x <= centerX + 12 / 2; x++) {
            for (int z = centerZ - 12 / 2; z <= centerZ + 12 / 2; z++) {
                Location chunkLocation = new Location(location.getWorld(), x * 16, 0, z * 16);
                if (CityDatabase.hasClaim(x, z, location.getWorld())) {
                    if (!Objects.equals(Objects.requireNonNull(CityDatabase.getClaim(chunkLocation)).getCityName(), HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String parseFlagChange(char[] flagsOriginal, String change) {
        String flagsRaw = new String(flagsOriginal);
        change = change.replace("*", "abcefghjrstv");

        boolean isAdding = true;

        for (int i = 0; i < change.length(); i++) {
            char currentChar = change.charAt(i);

            // the first character must be either a + or a -
            if (i == 0 && currentChar != '+' && currentChar != '-') {
                return null;
            }

            if (currentChar == '+') {
                isAdding = true;
                continue;
            } else if (currentChar == '-') {
                isAdding = false;
                continue;
            }

            if (!isValidFlag(currentChar)) {
                return null;
            }

            flagsRaw = isAdding ? flagsRaw + currentChar : flagsRaw.replace(String.valueOf(currentChar), "");
        }

        StringBuilder flagsNew = new StringBuilder();

        for (char flag : flagsRaw.toCharArray()) {
            boolean exists = false;

            for (int j = 0; j < flagsNew.length(); j++) {
                if (flagsNew.charAt(j) == flag) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                flagsNew.append(flag);
            }
        }

        char[] flagsNewArray = flagsNew.toString().toCharArray();

        Arrays.sort(flagsOriginal);
        Arrays.sort(flagsNewArray);

        flagsNew = new StringBuilder(new String(flagsNewArray));

        // don't change if there's nothing to change
        if (flagsNew.toString().equals(new String(flagsOriginal))) {
            return null;
        }

        return flagsNew.toString();
    }

    private static boolean isValidFlag(char currentChar){
        return currentChar == 'a' || currentChar == 'b' || currentChar == 'c' || currentChar == 'e' || currentChar == 'f' || currentChar == 'g' || currentChar == 'h' || currentChar == 'j' || currentChar == 'r' || currentChar == 's' || currentChar == 't' || currentChar == 'v';
    }

    public static String polygonToString(Location[] polygon) {
        StringBuilder string = new StringBuilder();
        for (Location location : polygon) {
            string.append(locationToString(location)).append(" ");
        }
        return string.toString();
    }

    public static Location[] stringToPolygon(String string) {
        String[] split = string.split(" ");
        Location[] polygon = new Location[split.length / 6];
        for (int i = 0; i < split.length; i += 6) {
            polygon[i / 6] = stringToLocation(split[i] + " " + split[i + 1] + " " + split[i + 2] + " " + split[i + 3] + " " + split[i + 4] + " " + split[i + 5]);
        }
        return polygon;
    }

    public static void sendCityScoreboard(Player player, City city) {
        FastBoard board = new FastBoard(player);
        int i = 0;
        if (CityDatabase.getClaim(player.getLocation()) != null) {
            board.updateTitle("§a             §l" + city.getCityName() + "§r             ");
            board.updateLine(i, " ");
            i = i + 1;
            for (Plot plot : city.getCityPlots()) {
                Polygon polygon = new Polygon();
                for (Location loc : plot.getPlotPoints()) {
                    polygon.addPoint(loc.getBlockX(), loc.getBlockZ());
                }
                if (polygon.contains(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    if (plot.isKMarked()) {
                        board.updateLine(i, plugin.getMessage("messages.city.scoreboard.placeK"));
                    } else {
                        board.updateLine(i, plugin.getMessage("messages.city.scoreboard.place"));
                    }
                    board.updateLine(i+1,"§a" + plot.getPlotName());
                    i = i + 2;
                    board.updateLine(i, " ");
                    i = i + 1;
                    if (plot.getPlotType() != null) {
                        board.updateLine(i, plugin.getMessage("messages.city.scoreboard.type"));
                        board.updateLine(i+1,"§a" + plot.getPlotType());
                        board.updateLine(i+2," ");
                        i = i + 3;
                    }
                    if (plot.getPlotOwner() != null) {
                        board.updateLine(i, plugin.getMessage("messages.city.scoreboard.owner"));
                        board.updateLine(i+1,"§a" + plot.getPlotOwner());
                        board.updateLine(i+2," ");
                        i = i + 3;
                    }
                    if (plot.isForSale()) {
                        board.updateLine(i, plugin.getMessage("messages.city.scoreboard.price"));
                        board.updateLine(i+1,"§a" + plot.getPlotPrice());
                    }
                    return;
                }
            }


            board.updateLine(i, plugin.getMessage("messages.city.scoreboard.members"));
            i = i + 1;
            board.updateLine(i, "§a" + city.getCityMembers().size());
            i = i + 1;
            board.updateLine(i, " ");
            i = i + 1;
            board.updateLine(i, plugin.getMessage("messages.city.scoreboard.plots"));
            i = i + 1;
            board.updateLine(i, "§a" + city.getCityPlots().size());

        } else {
            board.updateTitle(plugin.getMessage("messages.city.scoreboard.nature"));
            board.updateLine(0,plugin.getMessage("messages.city.scoreboard.pvp_on"));
        }
    }

    public static void sendNatureScoreboard(Player player) {
        FastBoard board = new FastBoard(player);
        board.updateTitle(plugin.getMessage("messages.city.scoreboard.nature"));
        board.updateLine(0,plugin.getMessage("messages.city.scoreboard.pvp_on"));
    }

    public static ItemStack letterBanner(String letter,String lore) {
        String letterLower = letter.toLowerCase();
        ItemStack banner = new ItemStack(org.bukkit.Material.WHITE_BANNER);
        BannerMeta bannerMeta = (BannerMeta)banner.getItemMeta();
        bannerMeta.setDisplayName("§5§o" + lore);
        bannerMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS);

        if (Objects.equals(letterLower, "a") || Objects.equals(letterLower, "å")|| Objects.equals(letterLower, "ä")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "b")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));

        }
        if (Objects.equals(letterLower, "c")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "d")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "e")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "f")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "g")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "h")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL_MIRROR));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "i")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "j")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "k")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "l")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "m")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.TRIANGLES_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "n")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.TRIANGLE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "o") || Objects.equals(letterLower, "ö")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "p")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL_MIRROR));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "q")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL_MIRROR));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.SQUARE_BOTTOM_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "r")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.HALF_HORIZONTAL));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.STRIPE_CENTER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "s")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.RHOMBUS_MIDDLE));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.CURLY_BORDER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "t")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_CENTER));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "u")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "v")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.TRIANGLE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "w")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.TRIANGLE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.TRIANGLES_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_LEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_RIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "x")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.CROSS));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "y")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNRIGHT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.HALF_HORIZONTAL_MIRROR));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        if (Objects.equals(letterLower, "z")) {
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_TOP));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_DOWNLEFT));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.BLACK, PatternType.STRIPE_BOTTOM));
            bannerMeta.addPattern(new org.bukkit.block.banner.Pattern(DyeColor.WHITE, PatternType.BORDER));
        }
        banner.setItemMeta(bannerMeta);
        return banner;
    }

}
