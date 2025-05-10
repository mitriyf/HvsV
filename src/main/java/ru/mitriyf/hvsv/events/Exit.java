package ru.mitriyf.hvsv.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.utils.Utils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Exit implements Listener {
    private final HvsV plugin;
    private final ThreadLocalRandom rnd;
    private final Values values;
    private final Utils utils;
    public Exit(HvsV plugin) {
        this.plugin = plugin;
        this.rnd = plugin.getRnd();
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
    }
    @EventHandler
    public void quitPlayer(PlayerQuitEvent e) {
        kick(e.getPlayer(), e.getPlayer().getUniqueId(), null);
    }
    @EventHandler
    public void deathPlayer(PlayerDeathEvent e) {
        if (!e.getEntity().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        kick(e.getEntity(), e.getEntity().getUniqueId(), e.getEntity().getKiller());
        e.getDrops().clear();
    }
    private void kick(Player p, UUID uuid, Player killer) {
        if (values.getPlayers().containsKey(uuid)) {
            Game game = values.getRooms().get(values.getPlayers().get(uuid).getGame());
            if (killer != null) {
                if (game.getHunters().contains(killer.getUniqueId())) {
                    sendMessage(values.getKillVictim(), p, killer, game, "%hunter%", "%victim%");
                } else if (game.getVictims().contains(killer.getUniqueId())) {
                    sendMessage(values.getKillHunter(), p, killer, game, "%victim%", "%hunter%");
                }
            }
            if (game.getAxes().contains(uuid)) {
                game.getAxes().remove(uuid);
                game.getTask().add(Bukkit.getScheduler().runTaskLater(plugin, game::setAxe, values.getRespawnAxe() * 20L));
            }
            game.kickPlayer(p);
        }
    }
    private void sendMessage(List<String> killRole, Player p, Player killer, Game game, String targ, String targ2) {
        for (String msg : killRole) {
            msg = msg.replace(targ, killer.getName()).replace(targ2, p.getName());
            for (UUID p2 : game.getPlayers()) game.send(Bukkit.getPlayer(p2), p.getUniqueId(), msg);
        }
    }
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (e.getPlayer().isOp()) return;
        if (values.getPlayers().containsKey(e.getPlayer().getUniqueId())) {
            for (String msg : values.getNo()) utils.sendMessage(e.getPlayer(), msg);
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onArmorStand(PlayerArmorStandManipulateEvent e) {
        if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        Player p = e.getPlayer();
        String room = values.getPlayers().get(p.getUniqueId()).getGame();
        Game game = values.getRooms().get(room);
        Location loc1 = e.getRightClicked().getLocation().clone().add(values.getAlocX() + 0.1, values.getAlocY() - 1 + 0.3, values.getAlocZ());
        giveAxe(game, loc1, p);
        e.setCancelled(true);
    }
    @EventHandler
    public void onInventory(PlayerPickupItemEvent e) {
        if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        if (e.getItem().getItemStack().getType() == Material.valueOf(values.getAxe())) {
            String room = values.getPlayers().get(e.getPlayer().getUniqueId()).getGame();
            if (values.getRooms().get(room) != null) if (values.getRooms().get(room).getHunters().contains(e.getPlayer().getUniqueId()) || e.getItem().getItemStack().getItemMeta().getDisplayName().equals("&cLOCKED")) e.setCancelled(true);
        }
    }
    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!p.getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        if (check(e.getPlayer().getWorld(), e.getItem(), "click")) {
            e.setCancelled(true);
            String room = values.getPlayers().get(e.getPlayer().getUniqueId()).getGame();
            values.getRooms().get(room).kickPlayer(e.getPlayer());
            return;
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (values.getPlayers().containsKey(p.getUniqueId())) {
                Game game = values.getRooms().get(values.getPlayers().get(p.getUniqueId()).getGame());
                if (!game.isActive()) {
                    e.setCancelled(true);
                    return;
                }
                if (!game.isAxe()) return;
                Block b = e.getClickedBlock();
                Location loc1 = b.getLocation();
                giveAxe(game, loc1, p);
            }
        }
    }
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity().getWorld().getName().equalsIgnoreCase(values.getWorld())) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) e.setCancelled(true);
            else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if (values.getPlayers().containsKey(e.getEntity().getUniqueId())) {
                    Game game = values.getRooms().get(values.getPlayers().get(e.getEntity().getUniqueId()).getGame());
                    if (!game.isActive()) {
                        e.setCancelled(true);
                        e.getEntity().teleport(game.getSpawn(0));
                    }
                }
            }
        }
    }
    @EventHandler
    public void onBlock(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player)) {
            return;
        }
        if (e.getEntity().getWorld().getName().equalsIgnoreCase(values.getWorld())) {
            Player p = (Player) e.getEntity();
            String room = values.getPlayers().get(p.getUniqueId()).getGame();
            ItemStack hand = ((Player) e.getDamager()).getItemInHand();
            Game game = values.getRooms().get(room);
            if (!game.isActive()) e.setCancelled(true);
            else if ((game.getHunters().contains(p.getUniqueId()) && game.getHunters().contains(e.getDamager().getUniqueId())) ||
                    (game.getVictims().contains(p.getUniqueId()) && game.getVictims().contains(e.getDamager().getUniqueId()))) e.setCancelled(true);
            else if (hand.getType() == Material.valueOf(values.getSword())) e.setDamage(values.getHunDamage());
            else if (hand.getType() == Material.valueOf(values.getAxe())) {
                e.setDamage(values.getVicDamage());
                if (p.getItemInHand().getType() == Material.DIAMOND_SWORD) {
                    ItemStack stack = p.getItemInHand().clone();
                    p.setItemInHand(values.getAirI());
                    p.getInventory().setItem(rnd.nextInt(0, 8), stack);
                }
            }
            else e.setDamage(0);
        }
    }
    @EventHandler
    public void onInventory(InventoryClickEvent e) {
        if (check(e.getWhoClicked().getWorld(), e.getCurrentItem(), "")) e.setCancelled(true);
    }
    @EventHandler
    public void food(FoodLevelChangeEvent e) {
        if (e.getEntity().getWorld().getName().equalsIgnoreCase(values.getWorld())) if (e.getFoodLevel() != 10) e.setCancelled(true);
    }
    @EventHandler
    public void onInventory(PlayerDropItemEvent e) {
        ItemStack stack = e.getItemDrop().getItemStack();
        if (stack.getType() == Material.valueOf(values.getAxe()) || stack.getType() == Material.valueOf(values.getSword())|| check(e.getPlayer().getWorld(), stack, "")) e.setCancelled(true);
    }
    @EventHandler
    public void onBlock(BlockPlaceEvent e) {
        if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }
    @EventHandler
    public void onBlock(BlockBreakEvent e) {
        if (!e.getPlayer().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }
    @EventHandler
    public void onBlock(BlockBurnEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void waterLava(BlockFromToEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(EntityChangeBlockEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockPhysicsEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockFadeEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(LeavesDecayEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void block(BlockMultiPlaceEvent e) {
        if (!e.getBlock().getWorld().getName().equalsIgnoreCase(values.getWorld())) return;
        e.setCancelled(true);
    }
    @EventHandler
    public void mobSpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.DROPPED_ITEM || e.getEntityType() == EntityType.ARMOR_STAND) return;
        if (e.getLocation().getWorld().getName().equalsIgnoreCase(values.getWorld())) e.setCancelled(true);
    }
    public boolean check(World world, ItemStack stack, String type) {
        if (!world.getName().equalsIgnoreCase(values.getWorld()) || stack == null) return false;
        if (stack.getType().toString().contains("SPAWNER")) return true;
        else if (!type.equals("click")) return stack.getType() == Material.valueOf(values.getSword());
        else if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) return stack.getType() == values.getExitI().getType() &&
                stack.getItemMeta().getDisplayName().equalsIgnoreCase(utils.hex(values.getExitI().getItemMeta().getDisplayName()));
        return false;
    }
    private void giveAxe(Game game, Location loc1, Player p) {
        loc1 = loc1.getBlock().getLocation();
        if (game.getActives().contains(loc1.getWorld().getName() + ":" + loc1.getX() + loc1.getY() + loc1.getZ())) {
            if (!game.getHunters().contains(p.getUniqueId())) {
                p.getInventory().setItem(0, values.getAxeI());
                game.getAxes().add(p.getUniqueId());
                game.unsetAxe(p.getName());
            }
        }
    }
}
