package ru.mitriyf.hvsv.game;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.model.*;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.values.Values;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final String map;
    private final HvsV plugin;
    private final Utils utils;
    private final String name;
    private final MapData info;
    private final Values values;
    private final GameManager gameManager;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private final Set<UUID> axes = new HashSet<>();
    private final Set<Location> actives = new HashSet<>();
    private final Set<ArmorStand> stands = new HashSet<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Map<UUID, MemberData> players = new HashMap<>();
    private final String[] searchGame = {"%game%", "%axe%", "%role%", "%amount%", "%max_players%"};
    private int foodLevel, exitTime, health, minTime, mediumTime, maxTime, min, medium, max;
    private boolean active = true, start = false, axe = false, fullSlots;
    private LocationsData locations;
    private String mapName;

    public Game(HvsV plugin, MapData mapData, String map, String name, Player player) {
        this.plugin = plugin;
        this.info = mapData;
        this.utils = plugin.getUtils();
        this.values = plugin.getValues();
        this.name = name;
        this.map = map;
        random = plugin.getRandom();
        gameManager = plugin.getGameManager();
        scheduler = plugin.getServer().getScheduler();
        setupSchematic(player);
    }

    private void setupSchematic(Player player) {
        fullSlots = info.isFullSlots();
        mapName = info.getName();
        health = info.getHealth();
        foodLevel = info.getFoodLevel();
        exitTime = info.getExitTime();
        min = info.getMin();
        medium = info.getMedium();
        max = info.getMax();
        minTime = info.getMinTime();
        mediumTime = info.getMediumTime();
        maxTime = info.getMaxTime();
        locations = new LocationsData(plugin, this, player);
    }

    public void addPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        gameManager.getWaiters().remove(uuid);
        gameManager.getPlayers().put(uuid, new PlayerData(plugin, player, name));
        MemberData memberData = new MemberData(player);
        String locale = utils.getLocale().player(player);
        memberData.setLocale(locale);
        memberData.setStatus(values.getSWait().getOrDefault(locale, values.getSWait().get("")).replace("%time%", "0:00"));
        Map<String, String> victimNames = values.getVictimName();
        memberData.setName(victimNames.getOrDefault(locale, victimNames.get("")));
        players.put(uuid, memberData);
        player.setFallDistance(0);
        if (!player.teleport(locations.getSpawn(false))) {
            close(true, true);
            return;
        }
        setDefault(player);
        sendMessage(player, values.getJoin(), info.getJoin(), searchGame, new String[]{name, "", "", String.valueOf(players.size()), String.valueOf(max)});
        if (players.size() >= max) {
            active = true;
        }
    }

    public void kickPlayer(Player player, boolean force, boolean isPluginStop) {
        if (!isPluginStop) {
            String[] strings = new String[]{name, "", "", String.valueOf(players.size() - 1), String.valueOf(max)};
            sendMessage(player, values.getQuit(), info.getQuit(), searchGame, strings);
            if (force) {
                sendMessage(player, values.getKicked(), info.getKicked(), searchGame, strings);
            } else {
                sendMessage(player, values.getEnd(), info.getEnd(), searchGame, strings);
            }
        }
        UUID uuid = player.getUniqueId();
        Map<UUID, PlayerData> gameManagerPlayers = gameManager.getPlayers();
        PlayerData data = gameManagerPlayers.get(uuid);
        if (data != null) {
            data.apply();
        }
        players.remove(uuid);
        if (!force && !isPluginStop) {
            scheduler.runTaskLater(plugin, () -> {
                gameManagerPlayers.remove(uuid);
            }, 5L);
        } else {
            gameManagerPlayers.remove(uuid);
        }
    }

    public void waitPlayers() {
        active = false;
        new BukkitRunnable() {
            int time = minTime;

            @Override
            public void run() {
                if (players.isEmpty()) {
                    close(true, false);
                    cancel();
                    return;
                } else if (time == 0) {
                    start();
                    cancel();
                    return;
                }
                if (players.size() < min) {
                    time = minTime;
                    for (MemberData memberData : players.values()) {
                        memberData.setStatus(values.getSStopped().getOrDefault(memberData.getLocale(), values.getSStopped().get("")).replace("%min_players%", String.valueOf(min)));
                    }
                } else {
                    time--;
                    for (MemberData memberData : players.values()) {
                        memberData.setStatus(values.getSWait().getOrDefault(memberData.getLocale(), values.getSWait().get("")).replace("%time%", String.valueOf(time)));
                    }
                    if (players.size() >= max) {
                        active = true;
                        if (time > maxTime) {
                            time = maxTime;
                        }
                    } else if (players.size() >= medium) {
                        active = false;
                        if (time > mediumTime) {
                            time = mediumTime;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    private void start() {
        active = true;
        start = true;
        setRoles();
        new BukkitRunnable() {
            int time = info.getEndTime();

            @Override
            public void run() {
                time--;
                int minutes = (int) Math.floor((double) time / 60);
                int seconds = time % 60;
                if (time <= 0) {
                    tasks.add(scheduler.runTaskLater(plugin, () -> close(false, false), info.getExitTime()));
                    cancel();
                }
                Set<MemberData> hunters = new HashSet<>(), victims = new HashSet<>();
                for (MemberData memberData : players.values()) {
                    memberData.setStatus(values.getSStart().getOrDefault(memberData.getLocale(), values.getSStart().get("")).replace("%time%", minutes + ":" + (seconds < 10 ? "0" : "") + seconds));
                    if (memberData.isSpectator()) {
                        continue;
                    }
                    if (memberData.isHunter()) {
                        hunters.add(memberData);
                    } else {
                        victims.add(memberData);
                    }
                }
                if (victims.isEmpty()) {
                    win(values.getSWinHunter(), hunters, values.getWinhunt(), info.getWinHunter());
                    cancel();
                } else if (hunters.isEmpty()) {
                    win(values.getSWinVictim(), victims, values.getWinvict(), info.getWinVictim());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    private void win(Map<String, String> status, Set<MemberData> winners, Map<String, List<Action>> winMessage, List<Action> infoMessage) {
        for (MemberData memberData : players.values()) {
            memberData.setStatus(status.getOrDefault(memberData.getLocale(), status.get("")));
            Player player = memberData.getPlayer();
            if (winners.contains(memberData)) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }
            sendMessage(player, winMessage, infoMessage);
        }
        tasks.add(scheduler.runTaskLater(plugin, () -> close(false, false), info.getExitTime()));
    }

    private void setRoles() {
        Collection<MemberData> memberDataCollection = players.values();
        countHunters(memberDataCollection);
        for (MemberData memberData : memberDataCollection) {
            Player player = memberData.getPlayer();
            if (memberData.isHunter()) {
                setRole(player, info.getHunterHealth(), true, values.getHunterName());
            } else {
                setRole(player, info.getVictimHealth(), false, values.getVictimName());
            }
        }
        for (Location loc : locations.getItems()) {
            double[] standLocation = info.getStandLocation();
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(standLocation[0], standLocation[1], standLocation[2]), EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            double[] rightFace = info.getRightFace();
            stand.setRightArmPose(new EulerAngle(Math.toRadians(rightFace[0]), Math.toRadians(rightFace[1]), Math.toRadians(rightFace[2])));
            stands.add(stand);
            double[] blockLocation = info.getBlockLocation();
            actives.add(loc.clone().add(blockLocation[0], blockLocation[1], blockLocation[2]).getBlock().getLocation());
        }
        tasks.add(scheduler.runTaskLater(plugin, this::setAxe, info.getAxeSpawn() * 20L));
        tasks.add(scheduler.runTaskLater(plugin, () -> {
            boolean fenceEnabled = info.isFenceEnabled();
            for (MemberData memberData : players.values()) {
                Player player = memberData.getPlayer();
                if (!fenceEnabled && memberData.isHunter()) {
                    player.setFallDistance(0);
                    player.teleport(locations.getSpawn(false));
                }
                sendMessage(player, values.getExitHun(), info.getExitHun(), searchGame, new String[]{name, "", "", String.valueOf(players.size()), String.valueOf(max)});
            }
            if (fenceEnabled) {
                locations.removeFence();
            }
        }, info.getHunterSpawn() * 20L));
    }

    @SuppressWarnings("deprecation")
    private void setRole(Player player, int health, boolean isHunter, Map<String, String> roleMap) {
        player.setMaxHealth(health);
        player.setHealth(health);
        player.setFoodLevel(info.getFoodLevel());
        if (isHunter) {
            utils.setSlots(player, values.getHunterSlots());
            if (fullSlots) {
                utils.setSlots(player, info.getDefaultSlots());
            }
        }
        player.setFallDistance(0);
        if (!player.teleport(locations.getSpawn(isHunter))) {
            close(true, false);
            return;
        }
        sendMessage(player, values.getRole(), info.getRole(), searchGame, new String[]{name, "", roleMap.getOrDefault(utils.getLocale().player(player), roleMap.get("")), String.valueOf(players.size()), String.valueOf(max)});
    }

    private void countHunters(Collection<MemberData> players) {
        List<MemberData> playerList = new ArrayList<>(players);
        for (int[] s : info.getPlayersHun()) {
            if (playerList.size() >= s[0]) {
                for (int i = 0; i < s[1]; i++) {
                    MemberData memberData = playerList.remove(random.nextInt(playerList.size()));
                    memberData.setHunter(true);
                    Map<String, String> hunterNames = values.getHunterName();
                    memberData.setName(hunterNames.getOrDefault(memberData.getLocale(), hunterNames.get("")));
                }
                return;
            }
        }
    }

    public void setAxe() {
        Collection<ItemStackData> valuesVictimSlots = values.getVictimSlots().values();
        Collection<ItemStackData> infoVictimSlots = info.getVictimSlots().values();
        List<ItemStack> victimWeapons = new ArrayList<>();
        if (valuesVictimSlots.isEmpty() && infoVictimSlots.isEmpty()) {
            victimWeapons.add(values.getAirStack());
        } else {
            utils.checkItems(infoVictimSlots, victimWeapons);
            utils.checkItems(valuesVictimSlots, victimWeapons);
        }
        utils.setStandHand(stands, victimWeapons.get(random.nextInt(victimWeapons.size())));
        axe = true;
        for (MemberData memberData : players.values()) {
            sendMessage(memberData.getPlayer(), values.getStartAxe(), info.getStartAxe());
        }
    }

    public void unSetAxe(String playerName) {
        utils.setStandHand(stands, values.getAirStack());
        axe = false;
        for (MemberData memberData : players.values()) {
            sendMessage(memberData.getPlayer(), values.getGetAxe(), info.getGetAxe(), searchGame, new String[]{name, playerName, "", String.valueOf(players.size()), String.valueOf(max)});
        }
    }

    @SuppressWarnings("deprecation")
    private void setDefault(Player player) {
        player.setFlying(false);
        player.setMaxHealth(health);
        player.setHealth(health);
        player.setFoodLevel(foodLevel);
        player.setAllowFlight(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(info.getGameMode());
        utils.setSlots(player, values.getDefaultSlots());
        if (fullSlots) {
            utils.setSlots(player, info.getDefaultSlots());
        }
    }

    public void close(boolean force, boolean isPluginStop) {
        actives.clear();
        for (MemberData memberData : new HashSet<>(players.values())) {
            kickPlayer(memberData.getPlayer(), force, isPluginStop);
        }
        players.clear();
        for (ArmorStand stand : stands) {
            stand.remove();
        }
        stands.clear();
        if (isPluginStop) {
            clearTasks();
        } else {
            scheduler.runTaskLater(plugin, this::clearTasks, 5L);
        }
        plugin.getServer().unloadWorld(name, false);
        gameManager.getRooms().remove(name);
        if (values.isDeleteWhenClosing()) {
            values.deleteDirectory(new File(name));
        }
    }

    private void clearTasks() {
        for (BukkitTask task : tasks) {
            utils.getTasks().remove(task.getTaskId());
            task.cancel();
        }
        tasks.clear();
    }

    public void tryToSpectator(Player player, Location location) {
        if (!info.isSpectatorEnabled()) {
            kickPlayer(player, false, false);
            return;
        }
        MemberData memberData = players.get(player.getUniqueId());
        if (memberData != null) {
            memberData.setSpectator(true);
            player.setGameMode(GameMode.SPECTATOR);
            player.setFallDistance(0);
            if (!player.teleport(location)) {
                close(true, true);
                return;
            }
            player.setFlySpeed(info.getSpectatorFlySpeed());
        }
    }

    public void sendMessage(Player player, Map<String, List<Action>> msg, List<Action> msgSchem, String[] s, String[] r) {
        tasks.add(utils.sendMessage(player, msg, s, r));
        tasks.add(utils.sendMessage(player, msgSchem, s, r));
    }

    public void sendMessage(Player player, Map<String, List<Action>> msg, List<Action> msgSchem) {
        tasks.add(utils.sendMessage(player, msg));
        tasks.add(utils.sendMessage(player, msgSchem));
    }
}
