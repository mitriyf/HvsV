package ru.mitriyf.hvsv.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.model.MapData;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.Values;

public class BlockListener implements Listener {
    private final Utils utils;
    private final Values values;
    private final GameManager gameManager;

    public BlockListener(HvsV plugin) {
        utils = plugin.getUtils();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR && action != Action.LEFT_CLICK_AIR) {
            return;
        }
        Player player = e.getPlayer();
        Game game = gameManager.getGame(player.getUniqueId());
        if (game != null) {
            ItemStack stack = player.getItemInHand();
            MapData info = game.getInfo();
            if (info != null) {
                if (utils.checkItemIsWeapon(stack, false, values.getDefaultSlots().values(), info.getDefaultSlots().values())) {
                    e.setCancelled(true);
                    game.kickPlayer(player, false, false);
                    return;
                } else if (!game.isActive()) {
                    e.setCancelled(true);
                    return;
                }
                Block block = e.getClickedBlock();
                if (block != null) {
                    utils.checkAxe(player, game, info, block);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if (startWithWorldAndOp(block, player)) {
            MapData info = getInfo(block);
            if (info != null && !info.isPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            Player player = e.getPlayer();
            Game game = gameManager.getGame(block.getWorld().getName());
            if (game != null) {
                MapData info = game.getInfo();
                if (!player.hasPermission("hvsv.admin") && info != null && !info.isBreakBlock()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isBurnBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isIgniteBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isFromToBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isEntityChangeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isPhysicsBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isFadeBlock()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isLeavesDecay()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        Block block = e.getBlock();
        if (startWithWorld(block)) {
            MapData info = getInfo(block);
            if (info != null && !info.isMultiPlaceBlock()) {
                e.setCancelled(true);
            }
        }
    }

    private MapData getInfo(Block block) {
        Game game = gameManager.getGame(block.getWorld().getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(Block b) {
        return b.getWorld().getName().startsWith(values.getWorldStart());
    }

    private boolean startWithWorldAndOp(Block b, Player p) {
        return startWithWorld(b) && !p.isOp();
    }
}
