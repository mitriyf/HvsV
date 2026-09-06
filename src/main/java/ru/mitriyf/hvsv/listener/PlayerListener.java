package ru.mitriyf.hvsv.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.model.MapData;
import ru.mitriyf.hvsv.model.MemberData;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.values.Values;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {
    private final Utils utils;
    private final HvsV plugin;
    private final Values values;
    private final GameManager gameManager;
    private final ThreadLocalRandom random;
    private final BukkitScheduler scheduler;
    private final String[] searchGame = new String[]{"%game%", "%map%", "%victim%", "%hunter%"};

    public PlayerListener(HvsV plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        random = plugin.getRandom();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
        scheduler = plugin.getServer().getScheduler();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        Entity entityDamager = e.getDamager();
        if (!(entity instanceof Player && entityDamager instanceof Player)) {
            return;
        }
        Player player = ((Player) entity);
        Player damager = ((Player) entityDamager);
        if (!values.isDamageWaiters() && (gameManager.getWaiters().contains(damager.getUniqueId()) || gameManager.getWaiters().contains(player.getUniqueId()))) {
            e.setCancelled(true);
        }
        checkSpecialDamage(entity.getWorld(), player, damager, e);
    }

    @SuppressWarnings("deprecation")
    private void checkSpecialDamage(World world, Player player, Player damager, EntityDamageEvent event) {
        if (!startWithWorld(world)) {
            return;
        }
        Game game = gameManager.getGame(world.getName());
        if (game == null) {
            return;
        } else if (!game.isActive()) {
            event.setCancelled(true);
        }
        Map<UUID, MemberData> players = game.getPlayers();
        MemberData damagerData = players.get(damager.getUniqueId());
        MemberData playerData = players.get(player.getUniqueId());
        if (damagerData == null || playerData == null) {
            return;
        }
        boolean isDamagerHunter = damagerData.isHunter();
        if (isDamagerHunter == playerData.isHunter()) {
            event.setCancelled(true);
        }
        ItemStack hand = damager.getItemInHand();
        MapData info = game.getInfo();
        if (info != null) {
            if (utils.checkItemIsWeapon(hand, true, values.getHunterSlots().values(), values.getVictimSlots().values(), info.getHunterSlots().values(), info.getVictimSlots().values())) {
                int damage;
                if (damagerData.isHunter()) {
                    damage = info.getHunterDamage();
                    event.setDamage(damage);
                } else {
                    damage = info.getVictimDamage();
                    event.setDamage(damage);
                    checkHitRules(info, player);
                }
                checkDamage(game, player, damager, damage, event);
            } else {
                event.setDamage(0);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void checkHitRules(MapData info, Player player) {
        if (info.isHitHunterInventory()) {
            ItemStack hand = player.getItemInHand().clone();
            int slot = random.nextInt(0, 8);
            Inventory inventory = player.getInventory();
            if (inventory.getItem(slot) == null) {
                player.setItemInHand(null);
                inventory.setItem(slot, hand);
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        Player player = e.getPlayer();
        Game game = getGame(player.getUniqueId());
        if (game != null) {
            MapData info = game.getInfo();
            if (info != null) {
                double[] blockLocation = info.getBlockLocation();
                Location location = e.getRightClicked().getLocation().clone().add(-blockLocation[0], -blockLocation[1], -blockLocation[2]).getBlock().getLocation();
                utils.checkAxe(player, game, info, location.getBlock());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();
        if (!(entity instanceof Player)) {
            return;
        }
        if (startWithWorld(world)) {
            Player player = (((Player) entity));
            Game game = getGame(player.getUniqueId());
            if (game != null) {
                MapData info = game.getInfo();
                if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                    if ((info != null && !info.isFallDamage()) || !game.isActive()) {
                        e.setCancelled(true);
                        return;
                    }
                    checkDamage(game, player, null, e.getDamage(), e);
                } else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (!game.isActive()) {
                        e.setCancelled(true);
                        player.setFallDistance(0);
                        player.teleport(game.getLocations().getSpawn(false));
                        return;
                    }
                    checkDamage(game, player, null, e.getDamage(), e);
                } else if (!game.isActive()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("hvsv.admin") || e.getMessage().equalsIgnoreCase("/hvsv exit")) {
            return;
        }
        UUID uuid = p.getUniqueId();
        if (gameManager.getPlayers().containsKey(uuid) || gameManager.getWaiters().contains(uuid)) {
            utils.sendMessage(p, values.getInGame());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (startWithWorld(player.getWorld())) {
            UUID uuid = player.getUniqueId();
            Game game = gameManager.getGame(uuid);
            if (game != null) {
                checkKiller(player, uuid, player.getKiller());
                e.getDrops().clear();
                Location location = player.getLocation();
                scheduler.runTaskLater(plugin, () -> {
                    player.spigot().respawn();
                    game.tryToSpectator(player, location);
                }, 1);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            Player player = e.getPlayer();
            if (startWithWorld(player.getWorld())) {
                Game game = gameManager.getGame(player.getUniqueId());
                if (game != null) {
                    e.setCancelled(true);
                    game.kickPlayer(player, false, false);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        gameManager.getWaiters().remove(uuid);
        BukkitTask task = gameManager.getTasks().get(uuid);
        if (task != null) {
            task.cancel();
        }
        if (startWithWorld(player.getWorld())) {
            Game game = getGame(uuid);
            if (game != null) {
                game.kickPlayer(player, false, false);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            MapData info = getInfo(world);
            if (info != null && !info.isDropItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        World world = e.getEntity().getWorld();
        if (startWithWorld(world)) {
            Game game = getGame(world);
            if (game != null) {
                MapData info = game.getInfo();
                if (info != null && e.getFoodLevel() != info.getFoodLevel() && !info.isFoodLevelChange()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            MapData info = getInfo(world);
            if (info != null && !info.isPickupItem()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        World world = e.getPlayer().getWorld();
        if (startWithWorld(world)) {
            MapData info = getInfo(world);
            if (info != null && !info.isConsumeItem()) {
                e.setCancelled(true);
            }
        }
    }

    private void checkKiller(Player player, UUID uuid, Player killer) {
        Game game = getGame(uuid);
        if (game != null) {
            MapData info = game.getInfo();
            if (info != null) {
                if (killer != null) {
                    MemberData memberData = game.getPlayers().get(killer.getUniqueId());
                    if (memberData != null) {
                        if (memberData.isHunter()) {
                            sendMessage(game, values.getKillVictim(), info.getKillVictim(), player, killer);
                        } else {
                            sendMessage(game, values.getKillHunter(), info.getKillHunter(), killer, player);
                        }
                    }
                }
                if (game.getAxes().remove(uuid)) {
                    game.getTasks().add(Bukkit.getScheduler().runTaskLater(plugin, game::setAxe, info.getAxeRespawn() * 20L));
                }
            }
        }
    }

    private void checkDamage(Game game, Player player, Player killer, double damage, Cancellable e) {
        if ((player.getHealth() - damage) <= 0) {
            if (killer != null) {
                checkKiller(player, player.getUniqueId(), killer);
            }
            game.tryToSpectator(player, player.getLocation());
            e.setCancelled(true);
        }
    }

    private void sendMessage(Game game, Map<String, List<Action>> killMessage, List<Action> infoKillMessage, Player victim, Player hunter) {
        for (MemberData memberData : game.getPlayers().values()) {
            game.sendMessage(memberData.getPlayer(), killMessage, infoKillMessage, searchGame, new String[]{game.getName(), game.getMapName(), victim.getName(), hunter.getName()});
        }
    }

    private MapData getInfo(World world) {
        Game game = gameManager.getGame(world.getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(World world) {
        return world.getName().startsWith(values.getWorldStart());
    }

    private Game getGame(World world) {
        return gameManager.getGame(world.getName());
    }

    private Game getGame(UUID uuid) {
        return gameManager.getGame(uuid);
    }
}
