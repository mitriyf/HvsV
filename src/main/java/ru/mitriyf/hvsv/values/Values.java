package ru.mitriyf.hvsv.values;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.values.player.PlayerData;

import java.io.*;
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
    private List<String> map = new ArrayList<>();
    private List<String> no = new ArrayList<>();
    private List<String> join = new ArrayList<>();
    private List<String> quit = new ArrayList<>();
    private List<String> end = new ArrayList<>();
    private List<String> role = new ArrayList<>();
    private List<String> winvict = new ArrayList<>();
    private List<String> winhunt = new ArrayList<>();
    private List<String> playersHun = new ArrayList<>();
    private List<String> exitLore = new ArrayList<>();
    private List<String> startAxe = new ArrayList<>();
    private List<String> listAxe = new ArrayList<>();
    private List<String> exitHun = new ArrayList<>();
    private List<String> help = new ArrayList<>();
    private List<String> noperm = new ArrayList<>();
    private List<String> killVictim = new ArrayList<>();
    private List<String> killHunter = new ArrayList<>();
    private List<String> kicked = new ArrayList<>();
    private ItemStack axeI;
    private ItemStack swordI;
    private ItemStack helmetI;
    private ItemStack airI;
    private ItemStack exitI;
    private String[] spawns;
    private String[] items;
    private String[] hunters;
    private String world;
    private String notfound;
    private String started;
    private String connect;
    private String victim;
    private String hunter;
    private String stopped;
    private String wait;
    private String start;
    private String win_victim;
    private String win_hunter;
    private String exitName;
    private String exitMaterial;
    private String axe;
    private String sword;
    private String helmet;
    private int vicHealth;
    private int radius;
    private double vicDamage;
    private int hunHealth;
    private double hunDamage;
    private int x1;
    private int x2;
    private int y;
    private int z1;
    private int z2;
    private int hunSpawn;
    private int spawnAxe;
    private int respawnAxe;
    private double alocX;
    private double alocY;
    private double alocZ;
    private double afaceX;
    private double afaceY;
    private double afaceZ;
    private int min_players;
    private int medium_players;
    private int max_players;
    private int minTime;
    private int mediumTime;
    private int maxTime;
    private int endTime;
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
            plugin.getLogger().warning("Схематики не найдены. Начинаю попытку загрузки схематик из плагина...");
            dir.mkdir();
            try {
                exportSchematics();
                plugin.getLogger().info("Загрузка успешно завершена.");
            } catch (Exception e) {
                plugin.getLogger().warning("Критическая ошибка.");
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
        String path = "schematics/";
        try {
            for (String s : files) {
                String fullPath = path + s;
                if (!(new File(plugin.getDataFolder(), fullPath)).exists()) {
                    plugin.saveResource(fullPath, true);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("An error occurred when loading the schematics.");
            e.printStackTrace();
        }
        plugin.getLogger().warning("You can download the schematics and upload them to the server on the official page of the resource.");
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
        map = settings.getStringList("map");
        ConfigurationSection game = settings.getConfigurationSection("game");
        if (game == null) {
            plugin.getLogger().warning("No section found in the configuration: settings.game");
            return;
        }
        playersHun = game.getStringList("playersHun");
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
        if (!game.getStringList("items.exit.lore").isEmpty()) exitLore = game.getStringList("items.exit.lore");
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
        help = cmd.getStringList("help");
        noperm = cmd.getStringList("noperm");
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
        no = game.getStringList("actions.no");
        kicked = game.getStringList("actions.kicked");
        end = game.getStringList("actions.end");
        join = game.getStringList("actions.join");
        quit = game.getStringList("actions.quit");
        role = game.getStringList("actions.role");
        winhunt = game.getStringList("actions.win_hunter");
        winvict = game.getStringList("actions.win_victim");
        startAxe = game.getStringList("actions.startAxe");
        listAxe = game.getStringList("actions.getAxe");
        exitHun = game.getStringList("actions.exitHun");
        killHunter = game.getStringList("actions.killHunter");
        killVictim = game.getStringList("actions.killVictim");
    }
    private void clear() {
        schematics.clear();
        category.clear();
        map.clear();
        help.clear();
        noperm.clear();
        no.clear();
        end.clear();
        join.clear();
        quit.clear();
        role.clear();
        winvict.clear();
        winhunt.clear();
        exitLore.clear();
    }
}
