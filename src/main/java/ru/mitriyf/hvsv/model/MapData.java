package ru.mitriyf.hvsv.model;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.values.Values;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

@Getter
public class MapData {
    private final Map<String, List<String>> directoriesMap = new HashMap<>();
    private final Map<Integer, ItemStackData> defaultSlots = new HashMap<>();
    private final Map<Integer, ItemStackData> hunterSlots = new HashMap<>();
    private final Map<Integer, ItemStackData> victimSlots = new HashMap<>();
    private final Map<String, String> category = new HashMap<>();
    private final List<int[]> playersHun = new ArrayList<>();
    private final List<String[]> mapList = new ArrayList<>();
    private final Set<double[]> fenceList = new HashSet<>();
    private final Values values;
    private final Logger logger;
    private final String mapId;
    private final Utils utils;
    private String name;
    private GameMode gameMode;
    private List<String> gameRules;
    private float spectatorFlySpeed;
    private String spawns, items, hunters;
    private List<Action> end = new ArrayList<>();
    private OffsetData victimOffset, hunterOffset;
    private List<Action> join = new ArrayList<>();
    private List<Action> quit = new ArrayList<>();
    private List<Action> role = new ArrayList<>();
    private List<Action> kicked = new ArrayList<>();
    private List<Action> getAxe = new ArrayList<>();
    private List<Action> exitHun = new ArrayList<>();
    private List<Action> startAxe = new ArrayList<>();
    private List<Action> winHunter = new ArrayList<>();
    private List<Action> winVictim = new ArrayList<>();
    private List<Action> killHunter = new ArrayList<>();
    private List<Action> killVictim = new ArrayList<>();
    private double[] standLocation, rightFace, blockLocation;
    private boolean fullSlots, pasteAir, foodLevelChange, pickupItem, consumeItem, creatureSpawn, fenceEnabled;
    private boolean dropItem, entityExplode, entityTarget, itemSpawn, placeBlock, breakBlock, igniteBlock, fromToBlock, fallDamage;
    private boolean fadeBlock, burnBlock, entityChangeBlock, physicsBlock, leavesDecay, multiPlaceBlock, hitHunterInventory, spectatorEnabled;
    private int foodLevel, exitTime, x, y, z, min, medium, max, minTime, mediumTime, maxTime, endTime, health, victimHealth, victimDamage, hunterHealth, hunterDamage, hunterSpawn, axeSpawn, axeRespawn;

    public MapData(Values values, YamlConfiguration cfg, File mapFile, String mapId) {
        this.values = values;
        this.mapId = mapId;
        logger = values.getLogger();
        utils = values.getUtils();
        setupSettings(cfg);
        ConfigurationSection messages = cfg.getConfigurationSection("messages");
        setupMessages(messages);
        setupSchematics(mapFile);
    }

    private void setupSettings(YamlConfiguration cfg) {
        ConfigurationSection schematicSection = cfg.getConfigurationSection("schematic");
        setupSchematicSection(schematicSection);
        ConfigurationSection gameSection = cfg.getConfigurationSection("game");
        setupGame(gameSection);
        ConfigurationSection rulesSection = cfg.getConfigurationSection("rules");
        setupRules(rulesSection);
        ConfigurationSection playerSection = cfg.getConfigurationSection("player");
        setupPlayer(playerSection);
        generateItems(values.getItemSlots());
    }

    private void setupSchematicSection(ConfigurationSection schematicSection) {
        name = schematicSection.getString("name");
        ConfigurationSection coords = schematicSection.getConfigurationSection("coords");
        x = coords.getInt("x");
        y = coords.getInt("y");
        z = coords.getInt("z");
        ConfigurationSection categorySection = schematicSection.getConfigurationSection("category");
        if (categorySection == null) {
            categorySection = schematicSection.createSection("categorySection");
        }
        for (String s : categorySection.getKeys(false)) {
            category.put(s, categorySection.getString(s));
        }
        hunters = schematicSection.getString("hunters");
        spawns = schematicSection.getString("spawns");
        items = schematicSection.getString("items");
        for (String s : schematicSection.getStringList("map")) {
            mapList.add(s.split(" "));
        }
        pasteAir = schematicSection.getBoolean("pasteAir");
        gameRules = schematicSection.getStringList("gameRules");
    }

    private void setupGame(ConfigurationSection gameSection) {
        ConfigurationSection players = gameSection.getConfigurationSection("players");
        min = players.getInt("min");
        medium = players.getInt("medium");
        max = players.getInt("max");
        ConfigurationSection waitTime = gameSection.getConfigurationSection("waitTime");
        minTime = waitTime.getInt("min");
        mediumTime = waitTime.getInt("medium");
        maxTime = waitTime.getInt("max");
        endTime = gameSection.getInt("endTime");
        for (String s : gameSection.getStringList("playersHun")) {
            String[] playerSplit = s.split(":");
            playersHun.add(new int[]{utils.formatInt(playerSplit[0]), utils.formatInt(playerSplit[1])});
        }
        ConfigurationSection roleSection = gameSection.getConfigurationSection("role");
        ConfigurationSection victimSection = roleSection.getConfigurationSection("victim");
        victimHealth = victimSection.getInt("health");
        victimDamage = victimSection.getInt("damage");
        hitHunterInventory = victimSection.getBoolean("hitHunterInventory");
        victimOffset = setupOffset(victimSection);
        ConfigurationSection hunterSection = roleSection.getConfigurationSection("hunter");
        hunterHealth = hunterSection.getInt("health");
        hunterDamage = hunterSection.getInt("damage");
        hunterSpawn = hunterSection.getInt("spawn");
        hunterOffset = setupOffset(hunterSection);
        ConfigurationSection spectatorSection = roleSection.getConfigurationSection("spectator");
        spectatorEnabled = spectatorSection.getBoolean("enabled");
        spectatorFlySpeed = (float) spectatorSection.getDouble("flySpeed") / 10;
        ConfigurationSection fenceSection = gameSection.getConfigurationSection("fence");
        fenceEnabled = fenceSection.getBoolean("break");
        for (String string : fenceSection.getStringList("list")) {
            fenceList.add(utils.toDouble(string));
        }
        ConfigurationSection axeSection = gameSection.getConfigurationSection("axe");
        axeSpawn = axeSection.getInt("spawn");
        axeRespawn = axeSection.getInt("respawn");
        ConfigurationSection armorStandSection = gameSection.getConfigurationSection("armorStand");
        blockLocation = utils.toDouble(armorStandSection.getString("blockLocation"));
        standLocation = utils.toDouble(armorStandSection.getString("standLocation"));
        rightFace = utils.toDouble(armorStandSection.getString("rightFace"));
    }

    private OffsetData setupOffset(ConfigurationSection roleSection) {
        ConfigurationSection spawnLocation = roleSection.getConfigurationSection("spawnLocation");
        double[] offsetX = utils.toDouble(spawnLocation.getString("offsetX"));
        double offsetY = spawnLocation.getDouble("offsetY");
        double[] offsetZ = utils.toDouble(spawnLocation.getString("offsetZ"));
        float[] offsetPitch = utils.toFloat(spawnLocation.getString("offsetPitch"));
        return new OffsetData(offsetX, offsetY, offsetZ, offsetPitch);
    }

    private void setupRules(ConfigurationSection rules) {
        foodLevelChange = rules.getBoolean("foodLevelChange");
        pickupItem = rules.getBoolean("pickupItem");
        consumeItem = rules.getBoolean("consumeItem");
        dropItem = rules.getBoolean("dropItem");
        fallDamage = rules.getBoolean("fallDamage");
        creatureSpawn = rules.getBoolean("creatureSpawn");
        entityExplode = rules.getBoolean("entityExplode");
        entityTarget = rules.getBoolean("entityTarget");
        itemSpawn = rules.getBoolean("itemSpawn");
        placeBlock = rules.getBoolean("placeBlock");
        breakBlock = rules.getBoolean("breakBlock");
        burnBlock = rules.getBoolean("burnBlock");
        igniteBlock = rules.getBoolean("igniteBlock");
        fromToBlock = rules.getBoolean("fromToBlock");
        entityChangeBlock = rules.getBoolean("entityChangeBlock");
        physicsBlock = rules.getBoolean("physicsBlock");
        fadeBlock = rules.getBoolean("fadeBlock");
        leavesDecay = rules.getBoolean("leavesDecay");
        multiPlaceBlock = rules.getBoolean("multiPlaceBlock");
    }

    private void setupPlayer(ConfigurationSection playerSection) {
        foodLevel = playerSection.getInt("foodLevel");
        health = playerSection.getInt("health");
        gameMode = GameMode.valueOf(playerSection.getString("gameMode"));
        fullSlots = playerSection.getBoolean("fullSlots");
        exitTime = playerSection.getInt("exitTime");
    }

    private void generateItems(FileConfiguration slotsConfig) {
        ConfigurationSection schematics = slotsConfig.getConfigurationSection("schematics");
        if (schematics == null) {
            schematics = slotsConfig.createSection("schematics");
        }
        ConfigurationSection items = schematics.getConfigurationSection("schematics." + mapId);
        if (items == null) {
            items = schematics.createSection("schematics." + mapId);
        }
        values.generateSlotRole(items, "player", defaultSlots);
        values.generateSlotRole(items, "victim", victimSlots);
        values.generateSlotRole(items, "hunter", hunterSlots);
    }

    private void setupMessages(ConfigurationSection msg) {
        ConfigurationSection actions = msg.getConfigurationSection("actions");
        end = getActionList(actions.getStringList("end"));
        kicked = getActionList(actions.getStringList("kicked"));
        join = getActionList(actions.getStringList("join"));
        quit = getActionList(actions.getStringList("quit"));
        role = getActionList(actions.getStringList("role"));
        killHunter = getActionList(actions.getStringList("killHunter"));
        killVictim = getActionList(actions.getStringList("killVictim"));
        exitHun = getActionList(actions.getStringList("exitHun"));
        startAxe = getActionList(actions.getStringList("startAxe"));
        getAxe = getActionList(actions.getStringList("getAxe"));
        winHunter = getActionList(actions.getStringList("winHunter"));
        winVictim = getActionList(actions.getStringList("winVictim"));
    }

    private void setupSchematics(File mapFile) {
        File[] directories = mapFile.listFiles();
        if (directories == null) {
            logger.warning("No directories were found in the map folder: " + mapId);
        } else {
            for (File directory : directories) {
                if (!directory.isDirectory()) {
                    continue;
                }
                String directoryName = directory.getName();
                File[] schematics = directory.listFiles();
                List<String> schematicList = new ArrayList<>();
                if (schematics == null) {
                    logger.warning("No schematics were found in the map.directory folder: " + mapId + "." + directoryName);
                } else {
                    for (File fileSchematic : schematics) {
                        String schematicName = fileSchematic.getName();
                        if (schematicName.contains(".schem")) {
                            String path = fileSchematic.getPath();
                            try {
                                utils.getSchematic().generate(path, fileSchematic);
                            } catch (Exception e) {
                                logger.warning("Error loading schematic. Error in " + mapId + "." + directoryName + "." + schematicName + ": " + e);
                            }
                            schematicList.add(path);
                        }
                    }
                }
                directoriesMap.put(directoryName, schematicList);
            }
        }
    }

    private List<Action> getActionList(List<String> s) {
        return values.getActionList(s);
    }
}
