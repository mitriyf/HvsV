package ru.mitriyf.hvsv.values.player;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class PlayerData {
    private final Player p;
    private final String game;
    private final Location loc;
    private final ItemStack[] contents;
    private final boolean allowFly;
    private final int foodLevel;
    private final double healthLevel;
    private final double health;
    private final GameMode gamemode;
    private final float exp;
    public PlayerData(Player p, String game) {
        this.p = p;
        this.game = game;
        loc = p.getLocation();
        contents = p.getInventory().getContents();
        allowFly = p.getAllowFlight();
        foodLevel = p.getFoodLevel();
        healthLevel = p.getHealthScale();
        health = p.getHealth();
        gamemode = p.getGameMode();
        exp = p.getExp();
    }
    public void apply() {
        p.getInventory().clear();
        p.getInventory().setHelmet(new ItemStack(Material.AIR));
        p.spigot().respawn();
        p.teleport(loc);
        p.getInventory().setContents(contents);
        p.setAllowFlight(allowFly);
        p.setFoodLevel(foodLevel);
        p.setHealthScale(healthLevel);
        p.setHealth(health);
        p.setGameMode(gamemode);
        p.setExp(exp);
    }
}
