package ru.mitriyf.hvsv.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.model.MapData;
import ru.mitriyf.hvsv.values.Values;

public class EntityListener implements Listener {
    private final CreatureSpawnEvent.SpawnReason reason = CreatureSpawnEvent.SpawnReason.CUSTOM;
    private final GameManager gameManager;
    private final Values values;

    public EntityListener(HvsV plugin) {
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            MapData info = getInfo(entity);
            if (info != null && e.getSpawnReason() != reason && !info.isCreatureSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(startWithWorld(e.getPlayer()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            MapData info = getInfo(entity);
            if (info != null && !info.isEntityExplode()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            MapData info = getInfo(entity);
            if (info != null && !info.isEntityTarget()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent e) {
        Entity entity = e.getEntity();
        if (startWithWorld(entity)) {
            MapData info = getInfo(entity);
            if (info != null && !info.isItemSpawn()) {
                e.setCancelled(true);
            }
        }
    }

    private MapData getInfo(Entity entity) {
        Game game = gameManager.getGame(entity.getWorld().getName());
        if (game == null) {
            return null;
        }
        return game.getInfo();
    }

    private boolean startWithWorld(Entity e) {
        return e.getWorld().getName().startsWith(values.getWorldStart());
    }
}
