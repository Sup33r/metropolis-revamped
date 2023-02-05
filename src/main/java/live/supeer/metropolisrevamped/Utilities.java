package live.supeer.metropolisrevamped;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import org.bukkit.*;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class Utilities {
    public static String formattedMoney(Integer money) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setGroupingUsed(true);
        return formatter.format(money).replace(",", " ");
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
                if (CityDatabase.hasClaim(x, z, location.getWorld().getName())) {
                    if (!Objects.equals(CityDatabase.getClaim(chunkLocation), HCDatabase.getHomeCity(player.getUniqueId().toString()))) {
                        return true;
                    }
                }
            }
        }
        return false;
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
