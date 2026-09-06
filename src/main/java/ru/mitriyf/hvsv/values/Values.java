package ru.mitriyf.hvsv.values;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.model.ItemStackData;
import ru.mitriyf.hvsv.model.MapData;
import ru.mitriyf.hvsv.updater.Updater;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.utils.actions.ActionType;
import ru.mitriyf.hvsv.utils.colors.Colorizer;
import ru.mitriyf.hvsv.utils.colors.impl.LegacyColorizer;
import ru.mitriyf.hvsv.utils.colors.impl.MiniMessageColorizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Getter
@Setter
public class Values {
    private final HvsV plugin;
    private final Logger logger;
    private final File slotsFile;
    private final Updater updater;
    private final File dataFolder;
    private final File configFile;
    private final ItemStack airStack;
    private final String schematicsDir = "schematics/";
    private final List<String> maps = new ArrayList<>();
    private final String[] files = new String[]{"hello.txt"};
    private final Map<String, String> sWait = new HashMap<>();
    private final Map<String, String> sStart = new HashMap<>();
    private final Map<String, String> sStopped = new HashMap<>();
    private final Map<String, List<Action>> end = new HashMap<>();
    private final Map<String, String> sWinVictim = new HashMap<>();
    private final Map<String, String> sWinHunter = new HashMap<>();
    private final Map<String, String> victimName = new HashMap<>();
    private final Map<String, String> hunterName = new HashMap<>();
    private final Map<String, List<Action>> exit = new HashMap<>();
    private final Map<String, List<Action>> join = new HashMap<>();
    private final Map<String, List<Action>> role = new HashMap<>();
    private final Map<String, List<Action>> help = new HashMap<>();
    private final Map<String, List<Action>> quit = new HashMap<>();
    private final Map<String, MapData> schematics = new HashMap<>();
    private final Map<String, List<Action>> noperm = new HashMap<>();
    private final Map<String, List<Action>> getAxe = new HashMap<>();
    private final Map<String, List<Action>> kicked = new HashMap<>();
    private final Map<String, List<Action>> waiter = new HashMap<>();
    private final Map<String, List<Action>> noExit = new HashMap<>();
    private final Map<String, List<Action>> inGame = new HashMap<>();
    private final Map<String, List<Action>> winvict = new HashMap<>();
    private final Map<String, List<Action>> winhunt = new HashMap<>();
    private final Map<String, List<Action>> exitHun = new HashMap<>();
    private final Map<String, List<Action>> connect = new HashMap<>();
    private final Map<String, List<Action>> started = new HashMap<>();
    private final Map<String, List<Action>> exitLore = new HashMap<>();
    private final Map<String, List<Action>> startAxe = new HashMap<>();
    private final Map<String, List<Action>> notfound = new HashMap<>();
    private final Map<String, List<Action>> killVictim = new HashMap<>();
    private final Map<String, List<Action>> killHunter = new HashMap<>();
    private final String[] lcs = new String[]{"de_DE", "en_US", "ru_RU"};
    private final Map<Integer, ItemStackData> hunterSlots = new HashMap<>();
    private final Map<Integer, ItemStackData> victimSlots = new HashMap<>();
    private final Map<Integer, ItemStackData> defaultSlots = new HashMap<>();
    private final Pattern action_pattern = Pattern.compile("\\[(\\w+)] ?(.*)");
    private boolean deleteWhenClosing, placeholderAPI, locale, miniMessage, damageWaiters;
    private boolean updaterEnabled = true, required = true, release = false;
    private ConfigurationSection settings;
    @Setter
    private FileConfiguration itemSlots;
    private FileConfiguration config;
    private String world, worldStart;
    @Setter
    private String defaultId = "";
    @Setter
    private String schematicUrl;
    private Colorizer colorizer;
    private Utils utils;
    private int amount;

    public Values(HvsV plugin) {
        this.plugin = plugin;
        updater = new Updater(plugin, this);
        dataFolder = plugin.getDataFolder();
        configFile = new File(dataFolder, "config.yml");
        slotsFile = new File(dataFolder, "slots.yml");
        logger = plugin.getLogger();
        airStack = new ItemStack(Material.AIR);
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            miniMessage = true;
        } catch (Exception e) {
            miniMessage = false;
        }
    }

    public void setup(boolean onlineUpdates) {
        getConfigurations();
        updater.checkUpdates(onlineUpdates);
        loadConfigurations();
        utils = plugin.getUtils();
        clear();
        setupSettings(onlineUpdates);
        setupLocales();
        setupSchematics();
        plugin.getSupports().register();
    }

    private void getConfigurations() {
        saveConfig("config", configFile, true);
        saveConfig("slots", slotsFile, false);
        loadConfigurations();
        if (settings == null) {
            return;
        }
        ConfigurationSection updater = settings.getConfigurationSection("updater");
        if (updater == null) {
            return;
        }
        updaterEnabled = updater.getBoolean("enabled");
        ConfigurationSection updaterSettings = updater.getConfigurationSection("settings");
        if (updaterSettings == null) {
            return;
        }
        required = updaterSettings.getBoolean("required");
        release = updaterSettings.getBoolean("release");
    }

    private void loadConfigurations() {
        config = YamlConfiguration.loadConfiguration(configFile);
        itemSlots = YamlConfiguration.loadConfiguration(slotsFile);
        settings = config.getConfigurationSection("settings");
    }

    private void setupSchematics() {
        File dir = new File(dataFolder, "schematics");
        if (!dir.exists()) {
            logger.warning("No schematics were found. I'm starting an attempt to download schematics from the plugin...");
            if (dir.mkdir()) {
                logger.info("The folder has been created.");
            }
            try {
                exportSchematics();
                logger.info("The download has been completed successfully.");
            } catch (Exception e) {
                logger.warning("A critical error. Error: " + e);
            }
        }
        File[] files = dir.listFiles();
        if (files == null) {
            logger.warning("No maps were found in the schematics folder.");
        } else {
            for (File mapFile : files) {
                if (!mapFile.isDirectory()) {
                    continue;
                }
                String mapName = mapFile.getName();
                String id = mapName.toLowerCase();
                if (id.equals("backups")) {
                    continue;
                }
                File file = new File(dir, mapName + ".yml");
                if (!file.exists()) {
                    plugin.saveResource(schematicsDir + "default.yml", true);
                    if (new File(dir, "default.yml").renameTo(file)) {
                        logger.info("The configuration file " + file.getName() + " has been created");
                    } else {
                        logger.warning("An error occurred while creating the " + file.getName() + " configuration file");
                        return;
                    }
                }
                maps.add(id);
                schematics.put(id, new MapData(this, YamlConfiguration.loadConfiguration(file), mapFile, id));
            }
        }
    }

    private void exportSchematics() {
        String path = dataFolder + "/" + schematicsDir;
        try {
            for (String s : files) {
                String fullPath = path + s;
                if (!(new File(fullPath)).exists()) {
                    plugin.saveResource(schematicsDir + s, true);
                }
            }
            InputStream in = new URL("https://github.com/mitriyf/HvsV/raw/refs/heads/main/downloads/" + schematicUrl).openStream();
            String fullPath = path + schematicUrl;
            Path fp = Paths.get(fullPath);
            Files.copy(in, fp, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            unpack(fullPath, path);
            Files.deleteIfExists(fp);
        } catch (Exception e) {
            logger.warning("An error occurred when loading the schematics. Check your internet connection.");
            logger.warning("You can download the schematics and upload them to the server on the official page of the resource. (GitHub)");
        }
    }

    private void setupSettings(boolean recovery) {
        String translate = settings.getString("translate").toLowerCase();
        if (miniMessage && translate.equalsIgnoreCase("minimessage")) {
            colorizer = new MiniMessageColorizer();
        } else {
            colorizer = new LegacyColorizer();
        }
        locale = settings.getBoolean("locales");
        ConfigurationSection games = settings.getConfigurationSection("games");
        world = games.getString("world");
        amount = games.getInt("amount");
        damageWaiters = games.getBoolean("damageWaiters");
        deleteWhenClosing = games.getBoolean("deleteWhenClosing");
        worldStart = world.replace("XIDX", "");
        ConfigurationSection supports = settings.getConfigurationSection("supports");
        placeholderAPI = supports.getBoolean("placeholderAPI");
        if (placeholderAPI && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            logger.warning("The PlaceholderAPI was not detected. This feature will be disabled.");
            placeholderAPI = false;
        }
        generateSlots();
        if (recovery) {
            recovery();
        }
    }

    private void generateSlots() {
        ConfigurationSection defaultSlotsSection = itemSlots.getConfigurationSection("default");
        if (defaultSlotsSection == null) {
            defaultSlotsSection = itemSlots.createSection("default");
        }
        generateSlotRole(defaultSlotsSection, "player", defaultSlots);
        generateSlotRole(defaultSlotsSection, "victim", victimSlots);
        generateSlotRole(defaultSlotsSection, "hunter", hunterSlots);
    }

    public void generateSlotRole(ConfigurationSection defaultSlots, String sectionName, Map<Integer, ItemStackData> itemStackDataMap) {
        ConfigurationSection itemSection = defaultSlots.getConfigurationSection(sectionName);
        if (itemSection == null) {
            itemSection = defaultSlots.createSection(sectionName);
        }
        for (String s : itemSection.getKeys(false)) {
            ConfigurationSection slot = itemSection.getConfigurationSection(s);
            ItemStack stack = setItemData(slot);
            ItemStackData itemStackData = new ItemStackData(stack);
            itemStackData.setExit(slot.getBoolean("exit"));
            itemStackData.setWeapon(slot.getBoolean("weapon"));
            itemStackDataMap.put(slot.getInt("slot"), itemStackData);
        }
    }

    private void setupLocales() {
        Map<String, FileConfiguration> locales = new HashMap<>();
        locales.put("", config);
        if (locale) {
            File file = new File(dataFolder, "locales");
            if (!file.exists()) {
                for (String s : lcs) {
                    plugin.saveResource("locales/" + s + ".yml", false);
                }
            }
            File[] dir = file.listFiles();
            if (dir == null) {
                logger.warning("Locales are empty.");
            } else {
                for (File f : dir) {
                    if (f.isFile()) {
                        String name = f.getName();
                        locales.put(name.substring(0, name.indexOf(".")).toLowerCase(), YamlConfiguration.loadConfiguration(f));
                    }
                }
            }
        }
        for (Map.Entry<String, FileConfiguration> entry : locales.entrySet()) {
            ConfigurationSection messages = entry.getValue().getConfigurationSection("messages");
            String name = entry.getKey();
            ConfigurationSection game = messages.getConfigurationSection("game");
            ConfigurationSection status = game.getConfigurationSection("status");
            setupStatusSection(name, status);
            ConfigurationSection role = game.getConfigurationSection("role");
            setupRoleSection(name, role);
            ConfigurationSection actions = game.getConfigurationSection("actions");
            ConfigurationSection commandSection = actions.getConfigurationSection("command");
            setupCommandSection(name, commandSection);
            ConfigurationSection roomSection = actions.getConfigurationSection("room");
            setupRoomSection(name, roomSection);
            ConfigurationSection gameSection = actions.getConfigurationSection("game");
            setupGameSection(name, gameSection);
        }
    }

    private void setupStatusSection(String name, ConfigurationSection statusSection) {
        sWait.put(name, statusSection.getString("wait"));
        sStart.put(name, statusSection.getString("start"));
        sStopped.put(name, statusSection.getString("stopped"));
        sWinVictim.put(name, statusSection.getString("winVictim"));
        sWinHunter.put(name, statusSection.getString("winHunter"));
    }

    private void setupRoleSection(String name, ConfigurationSection roleSection) {
        victimName.put(name, roleSection.getString("victim"));
        hunterName.put(name, roleSection.getString("hunter"));
    }

    private void setupCommandSection(String name, ConfigurationSection commandSection) {
        help.put(name, getActionList(commandSection.getStringList("help")));
        noperm.put(name, getActionList(commandSection.getStringList("noperm")));
    }

    private void setupRoomSection(String name, ConfigurationSection roomSection) {
        notfound.put(name, getActionList(roomSection.getStringList("notfound")));
        started.put(name, getActionList(roomSection.getStringList("started")));
        connect.put(name, getActionList(roomSection.getStringList("connect")));
        exit.put(name, getActionList(roomSection.getStringList("exit")));
        waiter.put(name, getActionList(roomSection.getStringList("waiter")));
        noExit.put(name, getActionList(roomSection.getStringList("noExit")));
    }

    private void setupGameSection(String name, ConfigurationSection gameSection) {
        inGame.put(name, getActionList(gameSection.getStringList("inGame")));
        kicked.put(name, getActionList(gameSection.getStringList("kicked")));
        end.put(name, getActionList(gameSection.getStringList("end")));
        join.put(name, getActionList(gameSection.getStringList("join")));
        quit.put(name, getActionList(gameSection.getStringList("quit")));
        role.put(name, getActionList(gameSection.getStringList("role")));
        killHunter.put(name, getActionList(gameSection.getStringList("killHunter")));
        killVictim.put(name, getActionList(gameSection.getStringList("killVictim")));
        exitHun.put(name, getActionList(gameSection.getStringList("exitHun")));
        startAxe.put(name, getActionList(gameSection.getStringList("startAxe")));
        getAxe.put(name, getActionList(gameSection.getStringList("getAxe")));
        winhunt.put(name, getActionList(gameSection.getStringList("winHunter")));
        winvict.put(name, getActionList(gameSection.getStringList("winVictim")));
    }

    private void recovery() {
        File dir = plugin.getServer().getWorldContainer().getAbsoluteFile();
        File[] list = dir.listFiles();
        if (list == null) {
            return;
        }
        for (File file : list) {
            if (file.getName().startsWith(worldStart)) {
                deleteDirectory(new File(file.getName()));
            }
        }
    }

    public void deleteDirectory(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    delete(file);
                }
            }
            delete(f);
        }
    }

    private ItemStack setItemData(ConfigurationSection slot) {
        return utils.generateItem(slot);
    }

    private void saveConfig(String configName, File file, boolean ignoreVersion) {
        if (file.exists()) {
            return;
        }
        String resource = configName + (ignoreVersion ? "" : defaultId) + ".yml";
        try {
            plugin.saveResource(resource, true);
            if (!defaultId.isEmpty()) {
                Path oldCfg = new File(dataFolder, resource).toPath();
                Files.move(oldCfg, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            logger.warning("Error save configurations. Error: " + e);
        }
    }

    private Action fromString(String str) {
        Matcher matcher = action_pattern.matcher(str);
        if (!matcher.matches()) {
            return new Action(ActionType.MESSAGE, str);
        }
        ActionType type;
        try {
            type = ActionType.valueOf(matcher.group(1).toUpperCase());
        } catch (IllegalArgumentException e) {
            type = ActionType.MESSAGE;
            return new Action(type, str);
        }
        return new Action(type, matcher.group(2).trim());
    }

    private void unpack(String zip, String dir) throws IOException {
        Path destDirPath = Paths.get(dir);
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zip)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = destDirPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    public List<Action> getActionList(List<String> actionStrings) {
        ImmutableList.Builder<Action> actionListBuilder = ImmutableList.builder();
        for (String actionString : actionStrings) {
            actionListBuilder.add(fromString(actionString));
        }
        return actionListBuilder.build();
    }

    private void clear() {
        if (plugin.getSupports() != null) {
            plugin.getSupports().unregister();
        }
        schematics.clear();
        maps.clear();
        hunterSlots.clear();
        victimSlots.clear();
        defaultSlots.clear();
        for (Map<String, List<Action>> actions : Arrays.asList(help, noperm, end, join, quit, role, winvict, startAxe, winhunt, exitLore, exitHun, kicked, killVictim, killHunter)) {
            actions.clear();
        }
    }

    public void backupConfig(String parentPath, File file, String oldVersion) throws IOException {
        File copied = new File(dataFolder, parentPath + "backups/" + file.getName() + "-" + oldVersion + ".backup");
        Path copiedPath = copied.toPath();
        Files.createDirectories(copied.getParentFile().toPath());
        Files.deleteIfExists(copiedPath);
        Files.copy(file.toPath(), copiedPath);
    }

    public void delete(File f) {
        try {
            Files.delete(f.toPath());
        } catch (IOException ignored) {
        }
    }
}
