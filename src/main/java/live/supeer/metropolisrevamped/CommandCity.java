package live.supeer.metropolisrevamped;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import co.aikar.idb.DB;
import live.supeer.metropolisrevamped.city.City;
import live.supeer.metropolisrevamped.city.CityDatabase;
import live.supeer.metropolisrevamped.city.Claim;
import live.supeer.metropolisrevamped.city.Member;
import live.supeer.metropolisrevamped.homecity.HCDatabase;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandAlias("city|c")
public class CommandCity extends BaseCommand {
  static MetropolisRevamped plugin;

  private CoreProtectAPI getCoreProtect() {
    Plugin corePlugin = plugin.getServer().getPluginManager().getPlugin("CoreProtect");

    // Check that CoreProtect is loaded
    if (!(corePlugin instanceof CoreProtect)) {
      return null;
    }

    // Check that the API is enabled
    CoreProtectAPI CoreProtect = ((CoreProtect) corePlugin).getAPI();
    if (!CoreProtect.isEnabled()) {
      return null;
    }

    // Check that a compatible version of the API is loaded
    if (CoreProtect.APIVersion() < 9) {
      return null;
    }

    return CoreProtect;
  }

  @Subcommand("info")
  @Default
  public static void onInfo(Player player, @Optional String cityName) {}

  @Subcommand("bank")
  public static void onBank(Player player, @Optional String[] args) {
    if (!player.hasPermission("metropolis.city.bank")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    Economy economy = MetropolisRevamped.getEconomy();
    if (args.length == 0) {
      if (HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      String homeCity = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());
      if (CityDatabase.getCity(homeCity).isEmpty()) {
        plugin.sendMessage(player, "messages.error.missing.city");
        return;
      }
      City city = CityDatabase.getCity(homeCity).get();
      String cityBalance = Utilities.formattedMoney(CityDatabase.getCityBalance(city));
      plugin.sendMessage(
          player, "messages.city.balance", "%balance%", cityBalance, "%cityname%", homeCity);
      return;
    }
    if (args[0].startsWith("+")) {
      if (args[0].substring(1).replaceAll("[0-9]", "").matches("[^0-9]")
          || args.length < 2
          || args[0].length() == 1) {
        plugin.sendMessage(player, "messages.syntax.city.bank.deposit");
        return;
      }

      int inputBalance = Integer.parseInt(args[0].replaceAll("[^0-9]", ""));
      String cityName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      String playerCity = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());

      if (CityDatabase.getCity(cityName).isEmpty()) {
        plugin.sendMessage(player, "messages.error.missing.city");
        return;
      }
      City city = CityDatabase.getCity(cityName).get();

      if (economy.getBalance(player) < inputBalance) {
        plugin.sendMessage(
            player, "messages.error.missing.playerBalance", "%cityname%", playerCity);
        return;
      }

      economy.withdrawPlayer(player, inputBalance);
      CityDatabase.addCityBalance(city, inputBalance);
      Database.addLogEntry(
          city,
          "{ \"type\": \"cityBank\", \"subtype\": \"deposit\", \"balance\": "
              + inputBalance
              + ", \"player\": "
              + player.getUniqueId().toString()
              + " }");
      plugin.sendMessage(
          player,
          "messages.city.successful.deposit",
          "%amount%",
          Utilities.formattedMoney(inputBalance),
          "%cityname%",
          cityName);
      return;
    }
    if (args[0].startsWith("-")) {
      if (args[0].substring(1).replaceAll("[0-9]", "").matches("[^0-9]")
          || args.length < 2
          || args[0].length() == 1) {
        plugin.sendMessage(player, "messages.syntax.city.bank.withdraw");
        return;
      }

      if (HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      int inputBalance = Integer.parseInt(args[0].replaceAll("[^0-9]", ""));
      if (CityDatabase.getCity(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))
          .isEmpty()) {
        plugin.sendMessage(player, "messages.error.missing.city");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      String inputBalanceFormatted = Utilities.formattedMoney(inputBalance);
      String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      assert city != null;
      int cityBalance = city.getCityBalance();
      String cityRole = CityDatabase.getCityRole(city, player.getUniqueId().toString());

      if (cityRole == null
          || cityRole.equals("member")
          || cityRole.equals("inviter")
          || cityRole.equals("assistant")) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      if (!(reason.length() >= 8)) {
        plugin.sendMessage(
            player, "messages.error.missing.reasonLength", "%cityname%", city.getCityName());
        return;
      }
      if (cityBalance <= 100000 || inputBalance > cityBalance - 100000) {
        plugin.sendMessage(
            player, "messages.error.missing.balance", "%cityname%", city.getCityName());
        return;
      }
      CityDatabase.removeCityBalance(city, inputBalance);
      Database.addLogEntry(
          city,
          "{ \"type\": \"cityBank\", \"subtype\": \"withdraw\", \"balance\": "
              + inputBalance
              + ", \"player\": "
              + player.getUniqueId().toString()
              + ", \"reason\": \""
              + reason
              + "\" }");
      economy.depositPlayer(player, inputBalance);
      plugin.sendMessage(
          player,
          "messages.city.successful.withdraw",
          "%amount%",
          inputBalanceFormatted,
          "%cityname%",
          city.getCityName());
      return;
    }
    plugin.sendMessage(player, "messages.syntax.city.bank.bank");
    plugin.sendMessage(player, "messages.syntax.city.bank.deposit");
    plugin.sendMessage(player, "messages.syntax.city.bank.withdraw");
  }

  @Subcommand("new")
  public static void onNew(Player player, String cityName) {
    Economy economy = MetropolisRevamped.getEconomy();
    if (!player.hasPermission("metropolis.city.new")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (CityDatabase.getPlayerCityCount(player.getUniqueId().toString()) >= 3) {
      plugin.sendMessage(player, "messages.error.city.maxCityCount");
      return;
    }
    if (economy.getBalance(player) < MetropolisRevamped.configuration.getCityCreationCost()) {
      plugin.sendMessage(player, "messages.error.city.missing.balance.cityCost");
      return;
    }
    if (cityName.length() < 1 || cityName.length() > 16) {
      plugin.sendMessage(player, "messages.error.city.nameLength");
      return;
    }
    if (CityDatabase.getCity(cityName).isPresent()) {
      plugin.sendMessage(player, "messages.error.city.cityExists");
      return;
    }
    if (CityDatabase.getClaim(player.getLocation()) != null) {
      plugin.sendMessage(player, "messages.error.city.claimExists");
      return;
    }
    if (Utilities.isCloseToOtherCity(player, player.getLocation(), "newcity")) {
      plugin.sendMessage(player, "messages.error.city.tooCloseToOtherCity");
      return;
    }

    City city = CityDatabase.newCity(cityName, player);
    assert city != null;
    Database.addLogEntry(
        city,
        "{ \"type\": \"create\", \"subtype\": \"city\", \"name\": "
            + city.getCityName()
            + ", \"tax\": "
            + MetropolisRevamped.configuration.getCityStartingTax()
            + ", \"spawn\": "
            + Utilities.formatLocation(city.getCitySpawn())
            + ", \"balance\": "
            + MetropolisRevamped.configuration.getCityStartingBalance()
            + ", \"player\": "
            + player.getUniqueId().toString()
            + " }");
    Database.addLogEntry(
        city,
        "{ \"type\": \"join\", \"subtype\": \"city\", \"player\": "
            + player.getUniqueId().toString()
            + " }");
    Database.addLogEntry(
        city,
        "{ \"type\": \"rank\", \"subtype\": \"change\", \"from\": "
            + "member"
            + ", \"to\": "
            + "mayor"
            + ", \"player\": "
            + player.getUniqueId().toString()
            + " }");
    Claim claim =
        CityDatabase.createClaim(
            city, player.getLocation(), false, player.getUniqueId().toString(), player.getName());
    MetropolisListener.playerInCity.put(player.getUniqueId(), city);
    Utilities.sendCityScoreboard(player, city);
    assert claim != null;
    Database.addLogEntry(
        city,
        "{ \"type\": \"buy\", \"subtype\": \"claim\", \"balance\": "
            + "0"
            + ", \"claimlocation\": "
            + Utilities.formatChunk(
                claim.getClaimWorld(), claim.getXPosition(), claim.getZPosition())
            + ", \"player\": "
            + player.getUniqueId().toString()
            + " }");
    economy.withdrawPlayer(player, MetropolisRevamped.configuration.getCityCreationCost());
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (onlinePlayer.hasPermission("metropolis.city.new")) {
        if (onlinePlayer == player) {
          plugin.sendMessage(
              onlinePlayer, "messages.city.successful.creation.self", "%cityname%", cityName);
        } else {
          plugin.sendMessage(
              onlinePlayer,
              "messages.city.successful.creation.others",
              "%playername%",
              player.getName(),
              "%cityname%",
              cityName);
        }
      }
    }
  }

  @Subcommand("claim")
  public static void onClaim(Player player, @Optional String mass) {
    if (!player.hasPermission("metropolis.city.claim")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
      plugin.sendMessage(player, "messages.error.missing.homeCity");
      return;
    }
    String cityName = HCDatabase.getHomeCityToCityname(player.getUniqueId().toString());
    if (CityDatabase.getClaim(player.getLocation()) != null) {
      plugin.sendMessage(player, "messages.error.city.claimExists");
      return;
    }
    if (CityDatabase.getCity(cityName).isEmpty()) {
      plugin.sendMessage(player, "messages.error.missing.city");
      return;
    }
    City city = CityDatabase.getCity(cityName).get();
    if (Utilities.isCloseToOtherCity(player, player.getLocation(), "city")) {
      plugin.sendMessage(player, "messages.error.city.tooCloseToOtherCity");
      return;
    }
    if (CityDatabase.getCityRole(city, player.getUniqueId().toString()) == null
        || Objects.equals(CityDatabase.getCityRole(city, player.getUniqueId().toString()), "member")
        || Objects.equals(
            CityDatabase.getCityRole(city, player.getUniqueId().toString()), "inviter")) {
      plugin.sendMessage(player, "messages.error.city.permissionDenied", "%cityname%", cityName);
      return;
    }
    Claim claim =
        CityDatabase.createClaim(
            city, player.getLocation(), false, player.getUniqueId().toString(), player.getName());
    assert claim != null;
    Database.addLogEntry(
        city,
        "{ \"type\": \"buy\", \"subtype\": \"claim\", \"balance\": "
            + "500"
            + ", \"claimlocation\": "
            + Utilities.formatChunk(
                claim.getClaimWorld(), claim.getXPosition(), claim.getZPosition())
            + ", \"player\": "
            + player.getUniqueId().toString()
            + " }");
    MetropolisListener.playerInCity.put(player.getUniqueId(), city);
    Utilities.sendCityScoreboard(player, city);
    CityDatabase.removeCityBalance(city, MetropolisRevamped.configuration.getCityClaimCost());
    plugin.sendMessage(
        player,
        "messages.city.successful.claim",
        "%cityname%",
        cityName,
        "%amount%",
        Utilities.formattedMoney(MetropolisRevamped.configuration.getCityClaimCost()));
  }

  @Subcommand("price")
  public static void onPrice(Player player) {
    if (player.hasPermission("metropolis.city.price")) {
      plugin.sendMessage(
          player,
          "messages.city.price",
          "%city%",
          Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.creationcost")),
          "%chunk%",
          Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.claimcost")),
          "%bonus%",
          Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.bonuscost")),
          "%go%",
          Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.citygocost")),
          "%outpost%",
          Utilities.formattedMoney(plugin.getConfig().getInt("settings.city.outpostcost")));
    } else {
      plugin.sendMessage(player, "messages.error.permissionDenied");
    }
  }

  @Subcommand("join")
  public static void onJoin(Player player, String cityname) {
    if (!player.hasPermission("metropolis.city.join")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (CityDatabase.getCity(cityname).isEmpty() || cityname == null) {
      plugin.sendMessage(player, "messages.error.missing.city");
      return;
    }
    City city = CityDatabase.getCity(cityname).get();
    if (!HCDatabase.hasHomeCity(player.getUniqueId().toString())
        && city.getCityName()
            .equals(HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()))) {
      plugin.sendMessage(player, "messages.error.city.alreadyInCity");
      return;
    }
    if (!city.isOpen() && !player.hasPermission("metropolis.admin.city.join")
        || !city.isOpen() && !invites.containsKey(player) && !invites.get(player).equals(city)) {
      plugin.sendMessage(player, "messages.error.city.closed", "%cityname%", city.getCityName());
      return;
    }
    if (Objects.requireNonNull(CityDatabase.memberCityList(player.getUniqueId().toString())).length
        >= 3) {
      plugin.sendMessage(player, "messages.error.city.maxCityCount");
      return;
    }
    if (CityDatabase.getCityRole(city, player.getUniqueId().toString()) != null) {
      plugin.sendMessage(player, "messages.error.city.alreadyInCity");
      return;
    }
    CityDatabase.newMember(city, player);
    invites.remove(player, city);
    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      if (CityDatabase.memberExists(player.getName(), city)) {
        if (onlinePlayer == player) {
          plugin.sendMessage(
              onlinePlayer, "messages.city.successful.join.self", "%cityname%", city.getCityName());
        } else {
          plugin.sendMessage(
              onlinePlayer,
              "messages.city.successful.join.others",
              "%playername%",
              player.getName(),
              "%cityname%",
              city.getCityName());
        }
      }
    }
  }

  private static final HashMap<Player, City> invites = new HashMap<>();
  private static final HashMap<HashMap<UUID, City>, Integer> inviteCooldownTime = new HashMap<>();
  private static final HashMap<HashMap<UUID, City>, BukkitRunnable> inviteCooldownTask =
      new HashMap<>();

  @Subcommand("invite")
  public static void onInvite(Player player, String invitee) {
    if (!player.hasPermission("metropolis.city.invite")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
        || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
      plugin.sendMessage(player, "messages.error.missing.homeCity");
      return;
    }
    @Deprecated Player inviteePlayer = Bukkit.getPlayer(invitee);
    if (inviteePlayer == null) {
      plugin.sendMessage(player, "messages.error.missing.player");
      return;
    }
    City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
    assert city != null;
    String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
    if (role == null) {
      plugin.sendMessage(
          player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
      return;
    }
    boolean isInviter =
        role.equals("inviter")
            || role.equals("assistant")
            || role.equals("vicemayor")
            || role.equals("mayor");
    if (!isInviter) {
      plugin.sendMessage(
          player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
      return;
    }
    HashMap<UUID, City> uuidCityHashMap =
        new HashMap<>() {
          {
            put(inviteePlayer.getUniqueId(), city);
          }
        };
    if (CityDatabase.getCityRole(city, inviteePlayer.getName()) != null) {
      plugin.sendMessage(player, "messages.error.city.alreadyInCity");
      return;
    }
    if (invites.containsKey(player) && invites.get(player).equals(city)) {
      plugin.sendMessage(
          player, "messages.error.city.invite.alreadyInvited", "%cityname%", city.getCityName());
      return;
    }
    if (inviteePlayer == player) {
      plugin.sendMessage(player, "messages.error.city.invite.self");
      return;
    }
    if (!inviteCooldownTime.containsKey(uuidCityHashMap)) {
      if (inviteCooldownTime.containsKey(uuidCityHashMap)) {
        if (inviteCooldownTime.get(uuidCityHashMap) > 0) {
          plugin.sendMessage(
              player,
              "messages.error.city.invite.cooldown",
              "%playername%",
              inviteePlayer.getName(),
              "%time%",
              inviteCooldownTime.get(uuidCityHashMap).toString());
          return;
        }
      }
      invites.put(inviteePlayer, city);
      plugin.sendMessage(
          player,
          "messages.city.invite.invited",
          "%player%",
          inviteePlayer.getName(),
          "%cityname%",
          city.getCityName(),
          "%inviter%",
          player.getName());
      plugin.sendMessage(
          inviteePlayer,
          "messages.city.invite.inviteMessage",
          "%cityname%",
          city.getCityName(),
          "%inviter%",
          player.getName());
      inviteCooldownTime.put(uuidCityHashMap, MetropolisRevamped.configuration.getInviteCooldown());
      inviteCooldownTask.put(
          uuidCityHashMap,
          new BukkitRunnable() {
            @Override
            public void run() {
              inviteCooldownTime.put(uuidCityHashMap, inviteCooldownTime.get(uuidCityHashMap) - 1);
              if (inviteCooldownTime.get(uuidCityHashMap) == 0) {
                inviteCooldownTime.remove(uuidCityHashMap);
                inviteCooldownTask.remove(uuidCityHashMap);
                invites.remove(inviteePlayer, city);
                cancel();
              }
            }
          });
      inviteCooldownTask.get(uuidCityHashMap).runTaskTimer(plugin, 20, 20);
    } else {
      plugin.sendMessage(
          player, "messages.error.city.invite.alreadyInvited", "%cityname%", city.getCityName());
    }
  }

  @Subcommand("go")
  public static void onGo(Player player, String[] args) {
    if (!player.hasPermission("metropolis.city.go")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
        || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
      plugin.sendMessage(player, "messages.error.missing.homeCity");
      return;
    }
    City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
    assert city != null;
    String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
    if (role == null) {
      plugin.sendMessage(
          player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
      return;
    }
    boolean isInviter =
        role.equals("inviter")
            || role.equals("assistant")
            || role.equals("vicemayor")
            || role.equals("mayor");
    boolean isAssistant =
        role.equals("assistant") || role.equals("vicemayor") || role.equals("mayor");
    boolean isViceMayor = role.equals("vicemayor") || role.equals("mayor");
    boolean isMayor = role.equals("mayor");
    if (args.length == 0
        || args.length == 1 && !args[0].replaceAll("[0-9]", "").matches("[^0-9].*")) {
      if (!player.hasPermission("metropolis.city.go.list")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (CityDatabase.getCityGoCount(city, role) == 0) {
        plugin.sendMessage(player, "messages.error.missing.goes");
        return;
      }
      if (args.length == 0) {
        args = new String[] {"1"};
      }
      if (args[0] == null) {
        args[0] = "1";
      }
      int goInt = Integer.parseInt(args[0]);
      StringBuilder tmpMessage = new StringBuilder();

      int itemsPerPage = 25;
      int start = (goInt * itemsPerPage) - itemsPerPage;
      int stop = goInt * itemsPerPage;
      if (Integer.parseInt(args[0]) < 1
          || Integer.parseInt(args[0])
              > (int)
                  Math.ceil(
                      ((double) CityDatabase.getCityGoCount(city, role))
                          / ((double) itemsPerPage))) {
        plugin.sendMessage(player, "messages.error.missing.page");
        return;
      }
      if (start >= CityDatabase.getCityGoCount(city, role)) {
        plugin.sendMessage(player, "messages.error.missing.page");
        return;
      }

      for (int i = start; i < stop; i++) {
        if (i == CityDatabase.getCityGoCount(city, role)) {
          break;
        }
        String name = Objects.requireNonNull(CityDatabase.getCityGoNames(city, role)).get(i);
        if (name == null) {
          break;
        }
        tmpMessage.append(name).append("§2,§a ");
      }
      plugin.sendMessage(
          player,
          "messages.list.goes",
          "%startPage%",
          String.valueOf(goInt),
          "%totalPages%",
          String.valueOf(
              (int)
                  Math.ceil(
                      ((double) CityDatabase.getCityGoCount(city, role))
                          / ((double) itemsPerPage))));
      player.sendMessage("§a" + tmpMessage.substring(0, tmpMessage.length() - 2));

    } else if (args.length == 2) {
      if (!CityDatabase.cityGoExists(args[0], city)) {
        plugin.sendMessage(player, "messages.error.missing.go", "%cityname%", city.getCityName());
        return;
      }
      if (!args[1].equals("delete")) {
        plugin.sendMessage(player, "messages.syntax.city.go");
        return;
      }
      if (!player.hasPermission("metropolis.city.go.delete")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (!CityDatabase.cityGoExists(args[0], city)) {
        plugin.sendMessage(player, "messages.error.missing.go", "%cityname%", city.getCityName());
        return;
      }

      String goAccessLevel = CityDatabase.getCityGoAccessLevel(args[0], city);
      if (goAccessLevel == null
          || goAccessLevel.equals("inviter")
          || goAccessLevel.equals("assistant")
          || goAccessLevel.equals("vicemayor")) {
        if (!isViceMayor) {
          plugin.sendMessage(
              player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
          return;
        }
        CityDatabase.deleteGo(args[0], city);
        Database.addLogEntry(
            city,
            "{ \"type\": \"delete\", \"subtype\": \"go\", \"name\": "
                + args[0]
                + ", \"player\": "
                + player.getUniqueId().toString()
                + " }");
        plugin.sendMessage(player, "messages.city.go.deleted", "%cityname%", city.getCityName());
      }

    } else if (args.length == 4) {
      if (!CityDatabase.cityGoExists(args[0], city)) {
        plugin.sendMessage(player, "messages.error.missing.go", "%cityname%", city.getCityName());
        return;
      }
      if (!args[1].equals("set")) {
        plugin.sendMessage(player, "messages.syntax.city.go");
        return;
      }
      String goAccessLevel = CityDatabase.getCityGoAccessLevel(args[0], city);
      if (args[2].equals("displayname")) {
        if (!isViceMayor) {
          plugin.sendMessage(
              player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
          return;
        }
        if (args[3].length() > 30) {
          plugin.sendMessage(
              player,
              "messages.error.city.go.invalidDisplayname",
              "%cityname%",
              city.getCityName());
          return;
        }
        if (Objects.equals(CityDatabase.getCityGoDisplayname(args[0], city), args[3])) {
          plugin.sendMessage(
              player,
              "messages.error.city.go.sameDisplayname",
              "%cityname%",
              city.getCityName(),
              "%name%",
              args[3]);
          return;
        }
        CityDatabase.setCityGoDisplayname(args[0], city, args[3]);
        plugin.sendMessage(
            player,
            "messages.city.go.changedDisplayname",
            "%cityname%",
            city.getCityName(),
            "%name%",
            args[0]);
      }
      if (args[2].equals("name")) {
        if (!isViceMayor) {
          plugin.sendMessage(
              player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
          return;
        }
        final String regex = "[^\\p{L}_0-9\\\\-]+";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(args[3]);
        if (matcher.find() || args[3].matches("^[0-9].*") || args[3].length() > 20) {
          plugin.sendMessage(player, "messages.error.city.go.invalidName");
          return;
        }
        if (CityDatabase.cityGoExists(args[3], city)) {
          plugin.sendMessage(
              player, "messages.error.city.go.alreadyExists", "%cityname%", city.getCityName());
          return;
        }
        CityDatabase.setCityGoName(args[0], city, args[3]);
        plugin.sendMessage(
            player,
            "messages.city.go.changedName",
            "%cityname%",
            city.getCityName(),
            "%name%",
            args[0]);
      }
      if (args[2].equals("accesslevel")) {
        if (!args[3].equals("-")
            && !args[3].equals("inviter")
            && !args[3].equals("assistant")
            && !args[3].equals("vicemayor")
            && !args[3].equals("mayor")) {
          plugin.sendMessage(
              player,
              "messages.error.city.go.invalidAccessLevel",
              "%cityname%",
              city.getCityName());
          return;
        }
        switch (args[3]) {
          case "-" -> {
            if (!isAssistant) {
              plugin.sendMessage(
                  player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
              return;
            }
            if (goAccessLevel == null) {
              plugin.sendMessage(
                  player,
                  "messages.error.city.go.alreadyAccessLevel",
                  "%cityname%",
                  city.getCityName());
              return;
            }
            CityDatabase.setCityGoAccessLevel(args[0], city, null);
            plugin.sendMessage(
                player,
                "messages.city.go.changedAccessLevel",
                "%cityname%",
                city.getCityName(),
                "%name%",
                args[0],
                "%accesslevel%",
                "Medlemmar");
            return;
          }
          case "inviter" -> {
            if (!isAssistant) {
              plugin.sendMessage(
                  player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
              return;
            }
            if (goAccessLevel != null && goAccessLevel.equals("inviter")) {
              plugin.sendMessage(
                  player,
                  "messages.error.city.go.alreadyAccessLevel",
                  "%cityname%",
                  city.getCityName());
              return;
            }
            CityDatabase.setCityGoAccessLevel(args[0], city, args[3]);
            plugin.sendMessage(
                player,
                "messages.city.go.changedAccessLevel",
                "%cityname%",
                city.getCityName(),
                "%name%",
                args[0],
                "%accesslevel%",
                "Inbjudare");
            return;
          }
          case "assistant" -> {
            if (!isAssistant) {
              plugin.sendMessage(
                  player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
              return;
            }
            if (goAccessLevel != null && goAccessLevel.equals("assistant")) {
              plugin.sendMessage(
                  player,
                  "messages.error.city.go.alreadyAccessLevel",
                  "%cityname%",
                  city.getCityName());
              return;
            }
            CityDatabase.setCityGoAccessLevel(args[0], city, args[3]);
            plugin.sendMessage(
                player,
                "messages.city.go.changedAccessLevel",
                "%cityname%",
                city.getCityName(),
                "%name%",
                args[0],
                "%accesslevel%",
                "Assistenter");
            return;
          }
          case "vicemayor" -> {
            if (!isViceMayor) {
              plugin.sendMessage(
                  player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
              return;
            }
            if (goAccessLevel != null && goAccessLevel.equals("vicemayor")) {
              plugin.sendMessage(
                  player,
                  "messages.error.city.go.alreadyAccessLevel",
                  "%cityname%",
                  city.getCityName());
              return;
            }
            CityDatabase.setCityGoAccessLevel(args[0], city, args[3]);
            plugin.sendMessage(
                player,
                "messages.city.go.changedAccessLevel",
                "%cityname%",
                city.getCityName(),
                "%name%",
                args[0],
                "%accesslevel%",
                "Vice Borgmästare");
            return;
          }
          case "mayor" -> {
            if (!isMayor) {
              plugin.sendMessage(
                  player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
              return;
            }
            if (goAccessLevel != null && goAccessLevel.equals("mayor")) {
              plugin.sendMessage(
                  player,
                  "messages.error.city.go.alreadyAccessLevel",
                  "%cityname%",
                  city.getCityName());
              return;
            }
            CityDatabase.setCityGoAccessLevel(args[0], city, args[3]);
            plugin.sendMessage(
                player,
                "messages.city.go.changedAccessLevel",
                "%cityname%",
                city.getCityName(),
                "%name%",
                args[0],
                "%accesslevel%",
                "Borgmästare");
            return;
          }
        }
      }
      plugin.sendMessage(player, "messages.syntax.city.go");
    } else if (args.length == 1) {
      if (!player.hasPermission("metropolis.city.go.teleport")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (!CityDatabase.cityGoExists(args[0], city)) {
        plugin.sendMessage(player, "messages.error.missing.go", "%cityname%", city.getCityName());
        return;
      }
      String goAccessLevel = CityDatabase.getCityGoAccessLevel(args[0], city);
      if (goAccessLevel == null) {
        Location location = CityDatabase.getCityGoLocation(args[0], city);
        assert location != null;
        player.teleport(location);
        // Istället för player.teleport här så ska vi ha en call till Mandatory, som sköter VIP
        // teleportering.
        return;
      }
      boolean hasAccess = false;
      switch (goAccessLevel) {
        case "mayor" -> {
          if (isMayor) {
            hasAccess = true;
          }
        }
        case "vicemayor" -> {
          if (isViceMayor) {
            hasAccess = true;
          }
        }
        case "assistant" -> {
          if (isAssistant) {
            hasAccess = true;
          }
        }
        case "inviter" -> {
          if (isInviter) {
            hasAccess = true;
          }
        }
      }
      if (!hasAccess) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      Location location = CityDatabase.getCityGoLocation(args[0], city);
      assert location != null;
      player.teleport(location);
      // Istället för player.teleport här så ska vi ha en call till Mandatory, som sköter VIP
      // teleportering.
    } else {
      plugin.sendMessage(player, "messages.syntax.city.go");
    }
  }

  @Subcommand("set")
  public static void onSet(Player player) {
    if (!player.hasPermission("metropolis.city.set")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    plugin.sendMessage(player, "messages.syntax.city.set.enter");
    plugin.sendMessage(player, "messages.syntax.city.set.exit");
    plugin.sendMessage(player, "messages.syntax.city.set.maxplotspermember");
    plugin.sendMessage(player, "messages.syntax.city.set.motd");
    plugin.sendMessage(player, "messages.syntax.city.set.name");
    plugin.sendMessage(player, "messages.syntax.city.set.spawn");
    plugin.sendMessage(player, "messages.syntax.city.set.tax");
  }

  @Subcommand("set")
  public class Set extends BaseCommand {

    @Subcommand("enter")
    public static void onEnter(Player player, String message) {
      if (!player.hasPermission("metropolis.city.set.enter")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
          || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      assert city != null;
      String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
      if (role == null) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      boolean isViceMayor = role.equals("mayor") || role.equals("vicemayor");
      if (!isViceMayor) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      if (message.equals("-")) {
        city.setEnterMessage(null);
        Database.addLogEntry(
            city,
            "{ \"type\": \"set\", \"subtype\": \"enterMessage\", \"message\": "
                + null
                + ", \"player\": "
                + player.getUniqueId().toString()
                + " }");
        plugin.sendMessage(
            player, "messages.city.successful.set.enter.removed", "%cityname%", city.getCityName());
        return;
      }
      if (message.length() > 225) {
        plugin.sendMessage(
            player,
            "messages.error.city.messageTooLong",
            "%cityname%",
            city.getCityName(),
            "%count%",
            "225");
        return;
      }
      city.setEnterMessage(message);
      Database.addLogEntry(
          city,
          "{ \"type\": \"set\", \"subtype\": \"enterMessage\", \"message\": "
              + message
              + ", \"player\": "
              + player.getUniqueId().toString()
              + " }");
      plugin.sendMessage(
          player, "messages.city.successful.set.enter.set", "%cityname%", city.getCityName());
    }

    @Subcommand("exit")
    public static void onExit(Player player, String message) {
      if (!player.hasPermission("metropolis.city.set.exit")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
          || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      assert city != null;
      String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
      if (role == null) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      boolean isViceMayor = role.equals("mayor") || role.equals("vicemayor");
      if (!isViceMayor) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      if (message.equals("-")) {
        city.setExitMessage(null);
        Database.addLogEntry(
            city,
            "{ \"type\": \"set\", \"subtype\": \"exitMessage\", \"message\": "
                + null
                + ", \"player\": "
                + player.getUniqueId().toString()
                + " }");
        plugin.sendMessage(
            player, "messages.city.successful.set.exit.removed", "%cityname%", city.getCityName());
        return;
      }
      if (message.length() > 225) {
        plugin.sendMessage(
            player,
            "messages.error.city.messageTooLong",
            "%cityname%",
            city.getCityName(),
            "%count%",
            "225");
        return;
      }
      city.setExitMessage(message);
      Database.addLogEntry(
          city,
          "{ \"type\": \"set\", \"subtype\": \"exitMessage\", \"message\": "
              + message
              + ", \"player\": "
              + player.getUniqueId().toString()
              + " }");
      plugin.sendMessage(
          player, "messages.city.successful.set.exit.set", "%cityname%", city.getCityName());
    }

    @Subcommand("motd")
    public static void onMotd(Player player, String message) {
      if (!player.hasPermission("metropolis.city.set.motd")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      assert city != null;
      String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
      if (role == null) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      boolean isViceMayor = role.equals("mayor") || role.equals("vicemayor");
      if (!isViceMayor) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      if (message.equals("-")) {
        city.setMotdMessage(null);
        Database.addLogEntry(
            city,
            "{ \"type\": \"set\", \"subtype\": \"motdMessage\", \"message\": "
                + null
                + ", \"player\": "
                + player.getUniqueId().toString()
                + " }");
        plugin.sendMessage(
            player, "messages.city.successful.set.motd.removed", "%cityname%", city.getCityName());
        return;
      }
      if (message.length() > 225) {
        plugin.sendMessage(
            player,
            "messages.error.city.messageTooLong",
            "%cityname%",
            city.getCityName(),
            "%count%",
            "225");
        return;
      }
      city.setMotdMessage(message);
      Database.addLogEntry(
          city,
          "{ \"type\": \"set\", \"subtype\": \"motdMessage\", \"message\": "
              + message
              + ", \"player\": "
              + player.getUniqueId().toString()
              + " }");
      plugin.sendMessage(
          player, "messages.city.successful.set.motd.set", "%cityname%", city.getCityName());
    }
  }

  @Subcommand("buy")
  public static void onBuy(Player player) {
    if (player.hasPermission("metropolis.city.buy")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    plugin.sendMessage(player, "messages.syntax.city.buy.bonus");
    plugin.sendMessage(player, "messages.syntax.city.buy.district");
    plugin.sendMessage(player, "messages.syntax.city.buy.go");
    plugin.sendMessage(player, "messages.syntax.city.buy.outpost");
  }

  @Subcommand("buy")
  public class Buy extends BaseCommand {

    @Subcommand("bonus")
    public static void onBonus(Player player, int count) {}

    @Subcommand("district")
    public static void onDistrict(Player player, String name) {}

    @Subcommand("go")
    public static void onGo(Player player, String name) {
      if (!player.hasPermission("metropolis.city.go")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
          || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      assert city != null;
      final String regex = "[^\\p{L}_0-9\\\\-]+";
      final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
      final Matcher matcher = pattern.matcher(name);
      if (matcher.find()
          || name.matches("^[0-9].*")
          || name.length() > 20
          || name.startsWith("-")) {
        plugin.sendMessage(
            player, "messages.error.city.go.invalidName", "%cityname%", city.getCityName());
        return;
      }
      String role = CityDatabase.getCityRole(city, player.getUniqueId().toString());
      if (role == null) {
        plugin.sendMessage(
            player, "messages.error.city.permissionDenied", "%cityname%", city.getCityName());
        return;
      }
      if (!player.hasPermission("metropolis.city.buy.go")) {
        plugin.sendMessage(player, "messages.error.permissionDenied");
        return;
      }
      if (city.getCityBalance() < MetropolisRevamped.configuration.getCityGoCost()) {
        plugin.sendMessage(
            player, "messages.error.city.missing.balance.goCost", "%cityname%", city.getCityName());
        return;
      }
      if (CityDatabase.cityGoExists(name, city)) {
        plugin.sendMessage(
            player, "messages.error.city.go.alreadyExists", "%cityname%", city.getCityName());
        return;
      }
      if (CityDatabase.getCityByClaim(player.getLocation()) != null) {
        plugin.sendMessage(player, "messages.error.city.go.outsideCity");
        return;
      }
      CityDatabase.newCityGo(player.getLocation(), name, city);
      city.removeCityBalance(MetropolisRevamped.configuration.getCityGoCost());
      Database.addLogEntry(
          city,
          "{ \"type\": \"buy\", \"subtype\": \"go\", \"name\": "
              + name
              + ", \"player\": "
              + player.getUniqueId().toString()
              + ", \"balance\": "
              + MetropolisRevamped.configuration.getCityGoCost()
              + ", \"claimlocation\": "
              + Utilities.formatLocation(player.getLocation())
              + " }");
      plugin.sendMessage(
          player, "messages.city.go.created", "%cityname%", city.getCityName(), "%name%", name);
    }

    @Subcommand("outpost")
    public static void onOutpost(Player player) {}
  }

  public static final List<Player> blockEnabled = new ArrayList<>();

  @Subcommand("block")
  public void onBlock(Player player, @Optional Integer page) {
    if (!player.hasPermission("metropolis.city.block")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (getCoreProtect() == null) {
      Bukkit.getLogger().severe("[Metropolis] CoreProtect not found.");
      player.sendMessage("§cSomething went wrong. Please contact an administrator.");
      return;
    }
    if (page == null) {
      if (blockEnabled.contains(player)) {
        blockEnabled.remove(player);
        MetropolisListener.savedBlockHistory.remove(player.getUniqueId());
        plugin.sendMessage(player, "messages.block.disabled");
        return;
      } else {
        blockEnabled.add(player);
        plugin.sendMessage(player, "messages.block.enabled");
      }
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
          || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
      }
    } else {
      if (!MetropolisListener.savedBlockHistory.containsKey(player.getUniqueId())) {
        plugin.sendMessage(player, "messages.error.missing.blockHistory");
        return;
      }
      if (page < 1) {
        plugin.sendMessage(player, "messages.error.missing.page");
        return;
      }
      int itemsPerPage = 8;
      int start = (page * itemsPerPage) - itemsPerPage;
      int stop = page * itemsPerPage;

      if (start >= MetropolisListener.savedBlockHistory.get(player.getUniqueId()).size()) {
        plugin.sendMessage(player, "messages.error.missing.page");
        return;
      }
      String[] firstLine = MetropolisListener.savedBlockHistory.get(player.getUniqueId()).get(0);
      CoreProtectAPI.ParseResult firstResult = getCoreProtect().parseResult(firstLine);
      player.sendMessage("");
      plugin.sendMessage(
          player,
          "messages.city.blockhistory.header",
          "%location%",
          "(["
              + firstResult.worldName()
              + "]"
              + firstResult.getX()
              + ","
              + firstResult.getY()
              + ","
              + firstResult.getZ()
              + ")",
          "%page%",
          page.toString(),
          "%totalpages%",
          String.valueOf(
              (int)
                  Math.ceil(
                      ((double)
                              MetropolisListener.savedBlockHistory.get(player.getUniqueId()).size())
                          / ((double) itemsPerPage))));
      for (int i = start; i < stop; i++) {
        if (i >= MetropolisListener.savedBlockHistory.get(player.getUniqueId()).size()) {
          break;
        }
        CoreProtectAPI.ParseResult result =
            getCoreProtect()
                .parseResult(MetropolisListener.savedBlockHistory.get(player.getUniqueId()).get(i));
        String row = "";
        int show = i + 1;
        if (result.getActionId() == 0) {
          row =
              "§2#"
                  + show
                  + " "
                  + result.getPlayer()
                  + " -- §c"
                  + result.getType().toString().toLowerCase().replace("_", " ")
                  + "§2 -- "
                  + Utilities.niceDate(result.getTimestamp() / 1000L);
        }
        if (result.getActionId() == 1) {
          row =
              "§2#"
                  + show
                  + " "
                  + result.getPlayer()
                  + " -- §a"
                  + result.getType().toString().toLowerCase().replace("_", " ")
                  + "§2 -- "
                  + Utilities.niceDate(result.getTimestamp() / 1000L);
        }
        if (result.getActionId() == 2) {
          row =
              "§2#"
                  + show
                  + " "
                  + result.getPlayer()
                  + " -- §e"
                  + result.getType().toString().toLowerCase().replace("_", " ")
                  + "§2 -- "
                  + Utilities.niceDate(result.getTimestamp() / 1000L);
        }
        if (!row.equals("")) {
          player.sendMessage(row);
        }
      }
    }
  }

  @Subcommand("helpop")
  public static void onHelpop(Player player, String message) {
    if (!player.hasPermission("metropolis.city.helpop")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
        || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
      plugin.sendMessage(player, "messages.error.missing.homeCity");
      return;
    }
    City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
    assert city != null;
    if (message.length() < 5) {
      plugin.sendMessage(player, "messages.error.message.tooShort");
      return;
    }
    int cityStaffOnline = 0;
    boolean isCityStaff =
        Objects.equals(
                CityDatabase.getCityRole(city, String.valueOf(player.getUniqueId())), "mayor")
            || Objects.equals(
                CityDatabase.getCityRole(city, String.valueOf(player.getUniqueId())), "vicemayor")
            || Objects.equals(
                CityDatabase.getCityRole(city, String.valueOf(player.getUniqueId())), "assistant");
    for (Player online : Bukkit.getOnlinePlayers()) {
      if (CityDatabase.memberExists(online.getName(), city) && isCityStaff) {
        cityStaffOnline++;
        plugin.sendMessage(
            online,
            "messages.city.helpop.receive",
            "%player%",
            player.getName(),
            "%message%",
            message);
      }
    }
    if (cityStaffOnline == 0) {
      plugin.sendMessage(player, "messages.city.helpop.noStaffOnline");
      return;
    }
    plugin.sendMessage(player, "messages.city.helpop.sent", "%message%", message);
  }

  @Subcommand("leave")
  public static void onLeave(Player player, String cityname) throws SQLException {
    if (!player.hasPermission("metropolis.city.leave")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
        || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
      plugin.sendMessage(player, "messages.error.missing.homeCity");
      return;
    }
    if (CityDatabase.getCity(cityname).isEmpty()) {
      plugin.sendMessage(player, "messages.error.missing.city");
    }
    City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
    if (!CityDatabase.memberExists(
        player.getUniqueId().toString(), CityDatabase.getCity(cityname).get())) {
      plugin.sendMessage(player, "messages.error.city.notInCity");
      return;
    }
    if (Objects.equals(
        CityDatabase.getCityRole(
            CityDatabase.getCity(cityname).get(), String.valueOf(player.getUniqueId())),
        "mayor")) {
      plugin.sendMessage(player, "messages.error.city.leave.mayor", "%cityname%", cityname);
      return;
    }
    assert city != null;
    city.removeCityMember(
        new Member(
            DB.getFirstRow(
                "SELECT * FROM `mp_members` WHERE `cityName` = "
                    + Database.sqlString(cityname)
                    + " AND `playerUUID` = "
                    + Database.sqlString(player.getUniqueId().toString())
                    + ";")));
    plugin.sendMessage(player, "messages.city.leave.success", "%cityname%", cityname);
    for (Player online : Bukkit.getOnlinePlayers()) {
      if (CityDatabase.memberExists(online.getName(), city)) {
        if (!online.getUniqueId().equals(player.getUniqueId())) {
          plugin.sendMessage(
              online,
              "messages.city.leave.message",
              "%player%",
              player.getName(),
              "%cityname%",
              cityname);
        }
      }
    }
  }

  @Subcommand("spawn")
  public static void onSpawn(Player player, @Optional String cityName) {
    if (!player.hasPermission("metropolis.city.spawn")) {
      plugin.sendMessage(player, "messages.error.permissionDenied");
      return;
    }
    if (cityName == null) {
      if (HCDatabase.hasHomeCity(player.getUniqueId().toString())
          || HCDatabase.getHomeCityToCityname(player.getUniqueId().toString()) == null) {
        plugin.sendMessage(player, "messages.error.missing.homeCity");
        return;
      }
      City city = HCDatabase.getHomeCityToCity(player.getUniqueId().toString());
      assert city != null;
      if (city.getCitySpawn() == null) {
        plugin.sendMessage(player, "messages.error.missing.spawn");
        return;
      }
      player.teleport(city.getCitySpawn());
      plugin.sendMessage(player, "messages.teleport", "%to%", "din hemstads startpunkt");
      return;
    }
    if (CityDatabase.getCity(cityName).isEmpty()) {
      plugin.sendMessage(player, "messages.error.missing.city");
      return;
    }
    City city = CityDatabase.getCity(cityName).get();
    if (city.getCitySpawn() == null) {
      plugin.sendMessage(player, "messages.error.missing.spawn");
      return;
    }
    if (!city.isOpen()) {
      plugin.sendMessage(player, "messages.error.city.closed", "%cityname%", city.getCityName());
      return;
    }
    player.teleport(city.getCitySpawn());
    plugin.sendMessage(player, "messages.teleport", "%to%", "startpunkten i " + city.getCityName());
  }
}
