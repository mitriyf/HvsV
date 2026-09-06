package ru.mitriyf.hvsv.model;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import ru.mitriyf.hvsv.HvsV;

import java.util.Collection;

@Getter
@SuppressWarnings("deprecation")
public class PlayerData {
    private final String game;
    private final HvsV plugin;
    private final Player player;
    private final Location location;
    private final GameMode gamemode;
    private final int foodLevel, level;
    private final boolean allowFly, flying;
    private final double health, healthMax;
    private final float exp, flySpeed, walkSpeed;
    private final ItemStack[] contents, armorContents;
    private final Collection<PotionEffect> potionEffects;

    public PlayerData(HvsV plugin, Player player, String game) {
        this.plugin = plugin;
        this.player = player;
        this.game = game;
        location = player.getLocation();
        contents = player.getInventory().getContents();
        armorContents = player.getInventory().getArmorContents();
        allowFly = player.getAllowFlight();
        flying = player.isFlying();
        foodLevel = player.getFoodLevel();
        healthMax = player.getMaxHealth();
        health = player.getHealth();
        gamemode = player.getGameMode();
        exp = player.getExp();
        potionEffects = player.getActivePotionEffects();
        level = player.getLevel();
        flySpeed = player.getFlySpeed();
        walkSpeed = player.getWalkSpeed();
        removeEffects();
    }

    public void apply() {
        removeEffects();
        player.getInventory().clear();
        player.setFallDistance(0);
        player.spigot().respawn();
        player.teleport(location);
        player.setGameMode(gamemode);
        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents);
        player.updateInventory();
        player.setAllowFlight(allowFly);
        player.setFlying(flying);
        player.setFoodLevel(foodLevel);
        player.setMaxHealth(healthMax);
        player.setHealth(health);
        player.setExp(exp);
        player.setLevel(level);
        player.setWalkSpeed(walkSpeed);
        player.setFlySpeed(flySpeed);
        for (PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }
    }

    private void removeEffects() {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
}
