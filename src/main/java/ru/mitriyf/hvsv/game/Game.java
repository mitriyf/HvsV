package ru.mitriyf.hvsv.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.player.PlayerData;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game {
    private final HvsV plugin;
    private final Utils utils;
    private final Values values;
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private final List<Location> spawns = new ArrayList<>();
    private final List<Location> items = new ArrayList<>();
    private final List<Location> huntersLoc = new ArrayList<>();
    private final List<BukkitTask> task = new ArrayList<>();
    private final List<UUID> players = new ArrayList<>();
    private final Set<UUID> axes = new HashSet<>();
    private final Set<EditSession> schematics = new HashSet<>();
    private final Set<UUID> hunters = new HashSet<>();
    private final Set<UUID> victims = new HashSet<>();
    private final Set<ArmorStand> stands = new HashSet<>();
    private final Set<String> actives = new HashSet<>();
    private final Map<String, List<String>> enters = new HashMap<>();
    private double default_x = 7;
    private double default_z = 7;
    private final int radius;
    private final String name;
    private final String map;
    private final BukkitWorld world;
    private boolean active = false;
    private boolean start = false;
    private boolean axe = false;
    private String status = "&eПодготовка...";
    public Game(HvsV plugin, String name, Player p) {
        this.plugin = plugin;
        this.utils = plugin.getUtils();
        this.values = plugin.getValues();
        this.name = name;
        this.map = values.getMaps().get(rnd.nextInt(values.getMaps().size()));
        for (Map.Entry<String, List<String>> entry : values.getEnter().entrySet()) enters.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        radius = values.getRadius();
        world = new BukkitWorld(Bukkit.getWorld(values.getWorld()));
        generateLocation();
        addPlayer(p);
        waitPlayers();
    }
    public void addPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        values.getPlayers().put(uuid, new PlayerData(p, name));
        p.teleport(getSpawn(0));
        players.add(uuid);
        p.getInventory().clear();
        p.getInventory().setItem(0, new ItemStack(values.getExitI()));
        for (UUID uid : players) {
            for (String msg : values.getJoin()) sendMessage(uid, msg.replace("%player%", p.getName())
                    .replace("%amount%", String.valueOf(players.size())));
        }
    }
    public void kickPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        for (UUID uid : players) {
            for (String msg : values.getQuit()) sendMessage(uid, msg.replace("%player%", p.getName())
                        .replace("%amount%", String.valueOf(players.size() - 1)));
        }
        players.remove(uuid);
        hunters.remove(uuid);
        victims.remove(uuid);
        values.getPlayers().get(uuid).apply();
        values.getPlayers().remove(uuid);
        for (String msg : values.getKicked()) sendMessage(p, msg);
    }
    private void waitPlayers() {
        new BukkitRunnable() {
            int time = values.getMinTime();
            @Override
            public void run() {
                if (players.isEmpty()) {
                    cancel();
                    close();
                    return;
                }
                if (time == 0) {
                    start();
                    cancel();
                }
                else if (players.size() < values.getMin_players()) {
                    time = values.getMinTime();
                    status = values.getStopped().replace("%min_players%", String.valueOf(values.getMin_players()));
                }
                else {
                    time--;
                    status = values.getWait().replace("%time%", String.valueOf(time));
                    if (players.size() >= values.getMax_players()) {
                        active = true;
                        if (time > values.getMaxTime()) {
                            time = values.getMaxTime();
                        }
                    }
                    else if (players.size() >= values.getMedium_players()) {
                        active = false;
                        if (time > values.getMediumTime()) {
                            time = values.getMediumTime();
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
            int time = values.getEndTime();
            @Override
            public void run() {
                time--;
                double m = Math.floor((double) time/60);
                double s1 = time%60;
                String s = String.valueOf(s1);
                if (s1 < 10) {
                    s = "0" + s1;
                }
                if (time <= 0) {
                    cancel();
                    for (UUID uuid : players) for (String msg : values.getEnd()) sendMessage(uuid, msg);
                    task.add(Bukkit.getScheduler().runTaskLater(plugin, () -> close(), 100));
                }
                status = values.getStart().replace("%time%", m + ":" + s);
                if (hunters.isEmpty()) {
                    cancel();
                    win(values.getWin_victim(), victims, values.getWinvict());
                } else if (victims.isEmpty()) {
                    cancel();
                    win(values.getWin_hunter(), hunters, values.getWinhunt());
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }
    private void win(String status, Set<UUID> winners, List<String> message) {
        this.status = status;
        for (UUID uuid : players) {
            if (winners.contains(uuid)) {
                Bukkit.getPlayer(uuid).setAllowFlight(true);
                Bukkit.getPlayer(uuid).setFlying(true);
            }
            for (String msg : message) sendMessage(uuid, msg);
        }
        task.add(Bukkit.getScheduler().runTaskLater(plugin, this::close, 100));
    }
    private void setRoles() {
        List<UUID> playerList = new ArrayList<>(players);
        amount(playerList);
        for (UUID victim : victims) setRole(victim, values.getVicHealth(), values.getAirI(), 0, values.getVictim());
        for (UUID hunter : hunters) {
            Player p = setRole(hunter, values.getHunHealth(), values.getSwordI(), 1, values.getHunter());
            p.getInventory().setHelmet(values.getHelmetI());
        }
        for (Location loc : items) {
            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(-0.1, -0.3, 0), EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            EulerAngle a = new EulerAngle(Math.toRadians(values.getAfaceX()),Math.toRadians(values.getAfaceY()),Math.toRadians(values.getAfaceZ()));
            stand.setRightArmPose(a);
            stands.add(stand);
            Block b = loc.clone().add(values.getAlocX(), values.getAlocY() - 1, values.getAlocZ()).getBlock();
            Location loc1 = b.getLocation();
            actives.add(loc1.getWorld().getName() + ":" + loc1.getX() + loc1.getY() + loc1.getZ());
        }
        task.add(Bukkit.getScheduler().runTaskLater(plugin, this::setAxe, values.getSpawnAxe() * 20L));
        task.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : hunters) Bukkit.getPlayer(uuid).teleport(getSpawn(0));
            for (UUID uid : players) for (String msg : values.getExitHun()) sendMessage(uid, msg);
        }, values.getHunSpawn() * 20L));
    }
    private Player setRole(UUID type, int health, ItemStack item, int spawn, String role) {
        Player p = Bukkit.getPlayer(type);
        if (p != null) {
            p.setHealth(20);
            p.setFoodLevel(10);
            p.setGameMode(GameMode.ADVENTURE);
        }
        p.setHealthScale(health);
        p.getInventory().setItem(0, item);
        p.teleport(getSpawn(spawn));
        for (String msg : values.getRole()) sendMessage(p, msg.replace("%role%", role));
        return p;
    }
    private void amount(List<UUID> playerList) {
        for (String s : values.getPlayersHun()) {
            String[] pi = s.split(":");
            if (playerList.size() >= Integer.parseInt(pi[0])) {
                for (int i = 0; i < Integer.parseInt(pi[1]); i++) {
                    UUID hunter = playerList.get(rnd.nextInt(playerList.size()));
                    hunters.add(hunter);
                    playerList.remove(hunter);
                }
                victims.addAll(playerList);
                return;
            }
        }
    }
    private void sendMessage(UUID uuid, String msg) {
        send(Bukkit.getPlayer(uuid), uuid, msg);
    }
    private void sendMessage(Player p, String msg) {
        send(p, p.getUniqueId(), msg);
    }
    public void send(Player p, UUID uuid, String msg) {
        if (msg.startsWith("[hunters] ")) {
            if (hunters.isEmpty() || hunters.contains(uuid)) utils.sendMessage(p, msg.replace("[hunters] ", ""));
        }
        else if (msg.startsWith("[victims] ")) {
            if (victims.isEmpty() || victims.contains(uuid)) utils.sendMessage(p, msg.replace("[victims] ", ""));
        }
        else utils.sendMessage(p, msg);
    }
    public void setAxe() {
        for (ArmorStand stand : stands) stand.setItemInHand(values.getAxeI());
        axe = true;
        for (UUID uid : players) for (String msg : values.getStartAxe()) sendMessage(Bukkit.getPlayer(uid), msg);
    }
    public void unsetAxe(String name) {
        for (ArmorStand stand : stands) stand.setItemInHand(values.getAirI());
        axe = false;
        for (UUID uid : players) for (String msg : values.getListAxe()) sendMessage(Bukkit.getPlayer(uid), msg.replace("%player%", name));
    }
    public Location getSpawn(int type) {
        Location loc;
        if (type == 1) loc = huntersLoc.get(rnd.nextInt(huntersLoc.size()));
        else loc = spawns.get(rnd.nextInt(spawns.size()));
        double r;
        double rz;
        if (type == 0) {
            r = rnd.nextDouble(default_x - 2) + 1;
            rz = rnd.nextDouble(default_x - 2) + 1;
        }
        else {
            r = rnd.nextDouble(default_x/2 - 3, default_x/2 + 3);
            rz = rnd.nextDouble(default_z/2 - 3, default_z/2 + 3);
        }
        Location spawn = loc.clone().add(r, 1, rz);
        Material minus = cloneLoc(spawn, 0, -1, 0);
        if (cloneLoc(spawn, 0, 2, 0) == Material.AIR && cloneLoc(spawn, 0, 1, 0) == Material.AIR && spawn.getBlock().getType() == Material.AIR && minus != Material.AIR && minus != Material.WATER && minus != Material.LAVA) {
            spawn.setYaw(rnd.nextInt(360));
            spawn.setPitch(rnd.nextInt(-35, 35));
            return spawn;
        }
        else return getSpawn(type);
    }
    private void generateLocation() {
        Block b = Bukkit.getWorld(values.getWorld()).getBlockAt(rnd.nextInt(values.getX1(), values.getX2()), values.getY(), rnd.nextInt(values.getZ1(), values.getZ2()));
        Location loc = b.getLocation();
        int r = values.getRadius();
        if (b.getType() == Material.AIR && cloneLoc(loc, -r, 0, -r) == Material.AIR && cloneLoc(loc, -r, 0, 0) == Material.AIR &&
                cloneLoc(loc, 0, 0, -r) == Material.AIR && cloneLoc(loc, r, 0, r) == Material.AIR &&
                cloneLoc(loc, r, 0, 0) == Material.AIR && cloneLoc(loc,0, 0, r) == Material.AIR) {
            double x = 0;
            double z = 0;
            for (String s : values.getMap()) {
                String[] type = s.split(" ");
                Vector v = paste(loc.clone().add(x, 0, 0), type[0]);
                default_x = v.getX();
                default_z = v.getZ();
                for (String value : type) {
                    if (z != 0) paste(loc.clone().add(x, 0, z), value);
                    z += default_z;
                }
                z = 0;
                x += default_x;
            }
        }
        else generateLocation();
    }
    private Material cloneLoc(Location spawn, int x, int y, int z) {
        return spawn.clone().add(x, y, z).getBlock().getType();
    }
    private Vector paste(Location loc, String s) {
        for (String q : values.getSpawns()) if (s.equals(q)) spawns.add(loc.clone());
        for (String q : values.getItems()) if (s.equals(q)) items.add(loc.clone().add(default_x / 2, 1, default_z / 2));
        for (String q : values.getHunters()) if (s.equals(q)) huntersLoc.add(loc.clone());
        s = values.getCategory().get(s).replace("%map%", map);
        String schematic;
        if (!enters.containsKey(s)) {
            List<String> schematics = values.getSchematics().get(s);
            int i = rnd.nextInt(schematics.size());
            schematic = schematics.get(i);
        } else {
            if (enters.get(s).isEmpty()) {
                enters.remove(s);
                enters.put(s, new ArrayList<>(values.getEnter().get(s)));
            }
            int r = rnd.nextInt(enters.get(s).size());
            String s2 = s + "/" + enters.get(s).get(r);
            enters.get(s).remove(r);
            List<String> schematics = values.getSchematics().get(s2);
            int i = rnd.nextInt(schematics.size());
            schematic = schematics.get(i);
            s = s2;
        }
        return utils.paste(loc, getSchematics(), s, schematic);
    }
    public void close() {
        for (UUID uuid : new ArrayList<>(players)) kickPlayer(Bukkit.getPlayer(uuid));
        for (EditSession session : new ArrayList<>(schematics)) session.undo(session);
        for (ArmorStand stand : new HashSet<>(stands)) stand.remove();
        for (BukkitTask task1 : new ArrayList<>(task)) task1.cancel();
        task.clear();
        actives.clear();
        values.getRooms().remove(name);
    }
}
