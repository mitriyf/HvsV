package ru.mitriyf.hvsv.values;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.values.player.PlayerData;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Getter
@Setter
public class Values {
    private final HvsV plugin;
    private final Map<String, Game> rooms = new HashMap<>();
    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final Map<String, List<String>> schematics = new HashMap<>();
    private final Map<String, List<String>> enter = new HashMap<>();
    private final List<String> maps = new ArrayList<>();
    private final Map<String, String> category = new HashMap<>();
    private final List<String> map = new ArrayList<>();
    private final List<String> no = new ArrayList<>();
    private final List<String> join = new ArrayList<>();
    private final List<String> quit = new ArrayList<>();
    private final List<String> end = new ArrayList<>();
    private final List<String> role = new ArrayList<>();
    private final List<String> winvict = new ArrayList<>();
    private final List<String> winhunt = new ArrayList<>();
    private final List<String> playersHun = new ArrayList<>();
    private final List<String> exitLore = new ArrayList<>();
    private final List<String> startAxe = new ArrayList<>();
    private final List<String> listAxe = new ArrayList<>();
    private final List<String> exitHun = new ArrayList<>();
    private final List<String> help = new ArrayList<>();
    private final List<String> noperm = new ArrayList<>();
    private final List<String> killVictim = new ArrayList<>();
    private final List<String> killHunter = new ArrayList<>();
    private final List<String> kicked = new ArrayList<>();
    private ItemStack axeI, swordI, helmetI, airI, exitI;
    private String[] spawns, items, hunters;
    private String world, notfound, started, connect, victim, hunter, stopped, wait, start, win_victim, win_hunter, exitName, exitMaterial, axe, sword, helmet;
    private int radius, vicHealth, hunHealth, x1, x2, y, z1, z2, hunSpawn, spawnAxe, respawnAxe;
    private double vicDamage, hunDamage, alocX, alocY, alocZ, afaceX, afaceY, afaceZ;
    private int min_players, medium_players, max_players, minTime, mediumTime, maxTime, endTime;
    public Values(HvsV plugin) {
        this.plugin = plugin;
    }
    public void setup() {
        clear();
        setupSettings();
        setupMessages();
        setupSchematics();
    }
    private void setupSchematics() {
        File dir = new File(plugin.getDataFolder(), "schematics");
        if (!dir.exists()) {
            plugin.getLogger().warning("No schematics were found. I'm starting an attempt to download schematics from the plugin...");
            dir.mkdir();
            try {
                exportSchematics();
                plugin.getLogger().info("The download has been completed successfully.");
            } catch (Exception e) {
                plugin.getLogger().warning("A critical error.");
                throw new RuntimeException(e);
            }
        }
        for (File fl : dir.listFiles()) {
            if (!fl.isDirectory()) continue;
            for (File fls : fl.listFiles()) {
                if (!fls.isDirectory()) continue;
                String dirName = "schematics/" + fl.getName() + "/" + fls.getName();
                maps.add(fl.getName());
                for (File flis : fls.listFiles()) {
                    if (flis.getName().contains("schem")) {
                        schematics.computeIfAbsent(dirName, map -> new ArrayList<>()).add(flis.getName());
                    } else if (flis.isDirectory() && flis.listFiles() != null) {
                        enter.computeIfAbsent(dirName, map -> new ArrayList<>()).add(flis.getName());
                        String end = "schematics/" + fl.getName() + "/" + fls.getName() + "/" + flis.getName();
                        for (File flim : flis.listFiles()) {
                            if (flim.getName().contains("schem")) {
                                schematics.computeIfAbsent(end, map -> new ArrayList<>()).add(flim.getName());
                            }
                        }
                    }
                }
            }
        }
    }
    private void exportSchematics() {
        String[] files = new String[] {"hello.txt"};
        String schematics = "schematics/";
        String path = plugin.getDataFolder() + "/" + schematics;
        try {
            for (String s : files) {
                String fullPath = path + s;
                if (!(new File(fullPath)).exists()) {
                    plugin.saveResource(schematics + s, true);
                }
            }
            InputStream in = new URL("https://github.com/jdevs-mc/HvsV/raw/refs/heads/main/schematics.zip").openStream();
            String fullPath = path + "schematics.zip";
            Path fp = Paths.get(fullPath);
            Files.copy(in, fp, StandardCopyOption.REPLACE_EXISTING);
            plugin.getUtils().unpack(fullPath, path);
            Files.deleteIfExists(fp);
        } catch (Exception e) {
            plugin.getLogger().warning("An error occurred when loading the schematics. Check your internet connection.");
            plugin.getLogger().warning("You can download the schematics and upload them to the server on the official page of the resource. (GitHub)");
            e.printStackTrace();
        }
    }
    private void setupSettings() {
        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("settings");
        if (settings == null) {
            plugin.getLogger().warning("No section found in the configuration: settings");
            return;
        }
        ConfigurationSection coords = settings.getConfigurationSection("coords");
        if (coords == null) {
            plugin.getLogger().warning("No section found in the configuration: settings.coords");
            return;
        }
        world = settings.getString("world");
        x1 = coords.getInt("x1"); x2 = coords.getInt("x2"); y = coords.getInt("y"); z1 = coords.getInt("z1"); z2 = coords.getInt("z2");
        radius = settings.getInt("radius", 50);
        for (String s : settings.getConfigurationSection("category").getKeys(false)) category.put(s, settings.getString("category." + s));
        spawns = settings.getString("spawns").split(" ");
        items = settings.getString("items").split(" ");
        hunters = settings.getString("hunters").split(" ");
        map.addAll(settings.getStringList("map"));
        ConfigurationSection game = settings.getConfigurationSection("game");
        if (game == null) {
            plugin.getLogger().warning("No section found in the configuration: settings.game");
            return;
        }
        playersHun.addAll(game.getStringList("playersHun"));
        min_players = game.getInt("players.min");
        medium_players = game.getInt("players.medium");
        max_players = game.getInt("players.max");
        minTime = game.getInt("waitTime.min");
        mediumTime = game.getInt("waitTime.medium");
        maxTime = game.getInt("waitTime.max");
        endTime = game.getInt("endTime", 241);
        exitName = game.getString("items.exit.name");
        exitMaterial = game.getString("items.exit.material");
        vicHealth = game.getInt("role.victim.health");
        hunHealth = game.getInt("role.hunter.health");
        hunSpawn = game.getInt("role.hunter.spawn");
        vicDamage = ((double) 20 / hunHealth) * game.getInt("role.victim.damage") + 0.01;
        hunDamage = ((double) 20 / vicHealth) * game.getInt("role.hunter.damage") + 0.01;
        if (!game.getStringList("items.exit.lore").isEmpty()) exitLore.addAll(game.getStringList("items.exit.lore"));
        axe = game.getString("items.victim.weapon");
        spawnAxe = game.getInt("items.victim.spawn");
        respawnAxe = game.getInt("items.victim.respawn");
        sword = game.getString("items.hunter.weapon");
        helmet = game.getString("items.hunter.helmet");
        alocX = game.getDouble("armorstand.loc.x");
        alocY = game.getDouble("armorstand.loc.y");
        alocZ = game.getDouble("armorstand.loc.z");
        afaceX = game.getDouble("armorstand.rightFace.x");
        afaceY = game.getDouble("armorstand.rightFace.y");
        afaceZ = game.getDouble("armorstand.rightFace.z");
    }
    private void setupMessages() {
        ConfigurationSection messages = plugin.getConfig().getConfigurationSection("messages");
        if (messages == null) {
            plugin.getLogger().warning("No section found in the configuration: messages");
            return;
        }
        ConfigurationSection cmd = messages.getConfigurationSection("cmd");
        if (cmd == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.cmd");
            return;
        }
        help.addAll(cmd.getStringList("help"));
        noperm.addAll(cmd.getStringList("noperm"));
        ConfigurationSection game = messages.getConfigurationSection("game");
        if (game == null) {
            plugin.getLogger().warning("No section found in the configuration: messages.game");
            return;
        }
        notfound = game.getString("room.notfound");
        started = game.getString("room.started");
        connect = game.getString("room.connect");
        victim = game.getString("role.victim");
        hunter = game.getString("role.hunter");
        stopped = game.getString("status.stopped");
        wait = game.getString("status.wait");
        start = game.getString("status.start");
        win_victim = game.getString("status.win_victim");
        win_hunter = game.getString("status.win_hunter");
        no.addAll(game.getStringList("actions.no"));
        kicked.addAll(game.getStringList("actions.kicked"));
        end.addAll(game.getStringList("actions.end"));
        join.addAll(game.getStringList("actions.join"));
        quit.addAll(game.getStringList("actions.quit"));
        role.addAll(game.getStringList("actions.role"));
        winhunt.addAll(game.getStringList("actions.win_hunter"));
        winvict.addAll(game.getStringList("actions.win_victim"));
        startAxe.addAll(game.getStringList("actions.startAxe"));
        listAxe.addAll(game.getStringList("actions.getAxe"));
        exitHun.addAll(game.getStringList("actions.exitHun"));
        killHunter.addAll(game.getStringList("actions.killHunter"));
        killVictim.addAll(game.getStringList("actions.killVictim"));
    }
    private void clear() {
        schematics.clear();
        enter.clear();
        category.clear();
        map.clear();
        maps.clear();
        for (List<String> strings : Arrays.asList(help, noperm, no, end, join, quit, role, playersHun, winvict, startAxe, listAxe, winhunt, exitLore, exitHun, kicked, killVictim, killHunter)) {
            strings.clear();
        }
    }
}
