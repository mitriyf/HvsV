package ru.mitriyf.hvsv;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.mitriyf.hvsv.cmd.CHvsV;
import ru.mitriyf.hvsv.events.Exit;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.supports.Placeholder;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.generator.EmptyWorld;
import ru.mitriyf.hvsv.values.Values;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public final class HvsV extends JavaPlugin {
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    private int version_mode = 13;
    private Values values;
    private Utils utils;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        setVersion();
        getLogger().info("Support: https://vk.com/jdevs");
        values = new Values(this);
        utils = new Utils(this);
        values.setup();
        utils.generateItems();
        getCommand("hvsv").setExecutor(new CHvsV(this));
        Bukkit.getPluginManager().registerEvents(new Exit(this), this);
        generate();
        placeholderAPI = new Placeholder(this);
        connectPlaceholderAPI();
    }
    public void generate() {
        if (version_mode >= 13) Bukkit.createWorld(new WorldCreator(values.getWorld()).generator(new EmptyWorld()));
        else {
            WorldCreator wc = new WorldCreator(values.getWorld());
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            wc.createWorld();
        }
        Bukkit.getWorld(values.getWorld()).setGameRuleValue("randomTickSpeed", "0");
    }
    Placeholder placeholderAPI = null;
    public void connectPlaceholderAPI() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    placeholderAPI.register();
                    getLogger().info("Connection to PlaceholderAPI was successful!");
                    cancel();
                }
            }
        }.runTaskTimer(this, 20, 20);
    }
    @Override
    public void onDisable() {
        if (placeholderAPI != null) placeholderAPI.unregister();
        for (Game game : new HashMap<>(values.getRooms()).values()) game.close();
        if (values.getWorld() != null) closeGame(values.getWorld());
    }
    private void setVersion() {
        String[] ver = getServer().getBukkitVersion().split("\\.");
        if (!ver[0].endsWith("1")) {
            getLogger().warning("THIS PLUGIN DOES NOT SUPPORT MINECRAFT VERSION >=2. Contact the developer to update the plugin.\n" +
                    "ДАННЫЙ ПЛАГИН НЕ ПОДДЕРЖИВАЕТ MINECRAFT ВЕРСИЮ >=2. Обратитесь к разработчику для обновления плагина.");
            return;
        }
        if (ver[1].length() >= 2) version_mode = Integer.parseInt(ver[1].substring(0, 2));
        else version_mode = Integer.parseInt(ver[1]);
        if (version_mode <= 7) {
            getLogger().info("Version mode: <=1.7.10");
            if (version_mode <= 6) getLogger().info("Version mode: <=?6?");
        } else if (version_mode <= 12) getLogger().info("Version mode: <=1.12.2");
        else getLogger().warning("The plugin cannot work correctly on 1.13 and higher.");
    }
    public void closeGame(String world) {
        if (Bukkit.getWorld(world) != null) {
            for (Player p : Bukkit.getWorld(world).getPlayers()) {
                if (p.isOnline()) {
                    p.setHealth(0);
                    p.spigot().respawn();
                    p.sendMessage(ChatColor.RED + "Game is closed.");
                }
            }
            Bukkit.unloadWorld(world, false);
        }
        try {
            Files.deleteIfExists(new File(world).toPath());
        } catch (IOException e) {
            getLogger().warning("Games closed incorrect.");
        }
    }
}

// Торт - это ложь.