package ru.mitriyf.hvsv.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class LocationsData {
    private final List<Location> huntersLoc = new ArrayList<>();
    private final List<Location> spawns = new ArrayList<>();
    @Getter
    private final List<Location> items = new ArrayList<>();
    private final BukkitScheduler scheduler;
    private final ThreadLocalRandom random;
    private final Logger logger;
    private final MapData info;
    private final HvsV plugin;
    private final String name;
    private final Utils utils;
    private final Game game;
    private World world;
    @Getter
    @Setter
    private Location defaultLocation;
    private double defaultX = 7, defaultZ = 7;

    public LocationsData(HvsV plugin, Game game, Player player) {
        this.plugin = plugin;
        this.game = game;
        scheduler = game.getScheduler();
        logger = plugin.getLogger();
        random = game.getRandom();
        utils = plugin.getUtils();
        info = game.getInfo();
        name = game.getName();
        generateLocation(player);
    }

    @SuppressWarnings("deprecation")
    private void generateLocation(Player player) {
        scheduler.runTask(plugin, () -> {
            world = plugin.getServer().getWorld(name);
            if (world == null) {
                world = utils.getWorldGenerator().generateWorld(name);
            }
            for (Entity e : world.getEntities()) {
                if (!(e instanceof Player) && !(e instanceof ItemFrame)) {
                    e.remove();
                }
            }
            for (String s : info.getGameRules()) {
                String[] gameRule = s.split(":");
                world.setGameRuleValue(gameRule[0], gameRule[1]);
            }
            defaultLocation = world.getBlockAt(info.getX(), info.getY(), info.getZ()).getLocation();
            scheduler.runTaskAsynchronously(plugin, () -> {
                double x = 0;
                double z = 0;
                for (String[] type : info.getMapList()) {
                    for (String value : type) {
                        VectorData vectorData = paste(defaultLocation.clone().add(x, 0, z), value);
                        defaultX = vectorData.getX();
                        defaultZ = vectorData.getZ();
                        z += defaultZ;
                    }
                    z = 0;
                    x += defaultX;
                }
                scheduler.runTask(plugin, () -> {
                    game.addPlayer(player);
                    game.waitPlayers();
                });
            });
        });
    }

    private VectorData paste(Location location, String s) {
        try {
            Location cloneLocation = location.clone();
            if (s.equals(info.getSpawns())) {
                spawns.add(cloneLocation);
            } else if (s.equals(info.getItems())) {
                items.add(cloneLocation.add(defaultX / 2, 1, defaultZ / 2));
            } else if (s.equals(info.getHunters())) {
                huntersLoc.add(cloneLocation);
            }
            List<String> schematics = info.getDirectoriesMap().get(info.getCategory().get(s));
            String schematic = schematics.get(random.nextInt(schematics.size()));
            return utils.paste(location, schematic, info.isPasteAir());
        } catch (Exception e) {
            logger.warning("Check that your configuration is correctly filled out. Error: " + e);
            game.close(true, true);
            return new VectorData(0, 0, 0);
        }
    }

    public Location getSpawn(boolean isHunter) {
        try {
            float yaw = random.nextInt(360);
            OffsetData offsetData;
            Location location;
            if (isHunter) {
                offsetData = info.getHunterOffset();
                location = huntersLoc.get(random.nextInt(huntersLoc.size())).clone();
            } else {
                offsetData = info.getVictimOffset();
                location = spawns.get(random.nextInt(spawns.size())).clone();
            }
            return utils.addOffsetLocation(offsetData, location, defaultX, defaultZ, yaw);
        } catch (Exception e) {
            logger.warning("Check that your configuration is correctly filled out. Error: " + e);
            game.close(true, true);
            return defaultLocation;
        }
    }

    public void removeFence() {
        boolean physic = info.isPhysicsBlock();
        for (Location hunterLocation : huntersLoc) {
            Location locationOne = hunterLocation.clone();
            Location locationTwo = hunterLocation.clone();
            for (double[] doubles : info.getFenceList()) {
                locationOne.add(doubles[0], doubles[1], doubles[2]);
                locationTwo.add(doubles[3], doubles[4], doubles[5]);
                utils.removeBlocks(world, locationOne, locationTwo, physic);
            }
        }
    }
}