package ru.mitriyf.hvsv.manager;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.model.MapData;
import ru.mitriyf.hvsv.model.PlayerData;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.Values;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {
    private final Utils utils;
    private final HvsV plugin;
    private final Values values;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private final String[] search = {"%room%"};
    @Getter
    private final Set<UUID> waiters = new HashSet<>();
    @Getter
    private final Map<String, Game> rooms = new HashMap<>();
    @Getter
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();
    @Getter
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public GameManager(HvsV plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        random = plugin.getRandom();
        values = plugin.getValues();
        scheduler = plugin.getServer().getScheduler();
    }

    public void join(Player player, String mapId) {
        UUID uuid = player.getUniqueId();
        if (players.containsKey(uuid) || waiters.contains(uuid)) {
            utils.sendMessage(player, values.getInGame());
            return;
        } else if (mapId != null) {
            if (!values.getSchematics().containsKey(mapId)) {
                utils.sendMessage(player, values.getNotfound());
                return;
            }
            for (Game game : rooms.values()) {
                if (!game.isActive() && game.getInfo().getMapId().equals(mapId)) {
                    utils.sendMessage(player, values.getConnect(), search, new String[]{game.getName()});
                    game.addPlayer(player);
                    return;
                }
            }
        } else {
            for (Game game : rooms.values()) {
                if (!game.isActive()) {
                    utils.sendMessage(player, values.getConnect(), search, new String[]{game.getName()});
                    game.addPlayer(player);
                    return;
                }
            }
        }
        generateRoom(player, uuid, mapId);
    }

    private void generateRoom(Player player, UUID uuid, String mapId) {
        String readyMapId = setMap(mapId);
        int amount = values.getAmount();
        String name = values.getWorldStart() + (amount < 1 ? 1 : random.nextInt(amount));
        if (amount > 0 && !rooms.containsKey(name)) {
            tasks.remove(uuid);
            utils.sendMessage(player, values.getConnect(), search, new String[]{name});
            MapData schematicData = values.getSchematics().get(readyMapId);
            waiters.add(uuid);
            rooms.put(name, null);
            scheduler.runTaskAsynchronously(plugin, () -> rooms.put(name, new Game(plugin, schematicData, readyMapId, name, player)));
        } else {
            regenerateRoom(player, uuid, mapId);
        }
    }

    private void regenerateRoom(Player player, UUID uuid, String mapId) {
        tasks.put(uuid, scheduler.runTaskLater(plugin, () -> {
            if (!waiters.contains(uuid)) {
                waiters.add(uuid);
                utils.sendMessage(player, values.getWaiter());
            }
            generateRoom(player, uuid, mapId);
        }, 10));
    }

    public Game getGame(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data != null) {
            String id = data.getGame();
            return rooms.get(id);
        }
        return null;
    }

    private String setMap(String mapId) {
        return mapId != null ? mapId : values.getMaps().get(random.nextInt(values.getMaps().size()));
    }

    public Game getGame(String world) {
        return rooms.get(world);
    }
}
