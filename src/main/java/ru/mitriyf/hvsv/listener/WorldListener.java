package ru.mitriyf.hvsv.listener;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;

import java.util.HashSet;
import java.util.Set;

public class WorldListener implements Listener {
    private final Set<String> unloaded = new HashSet<>();
    private final HvsV plugin;
    private final Values values;

    public WorldListener(HvsV plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }

    @EventHandler
    public void stopWorldSaving(WorldUnloadEvent e) {
        World w = e.getWorld();
        if (startWithWorld(w) && !unloaded.contains(w.getName())) {
            for (Chunk chunk : w.getLoadedChunks()) {
                chunk.unload(false);
            }
            w.setAutoSave(false);
            w.setKeepSpawnInMemory(false);
            unloaded.add(w.getName());
            e.setCancelled(true);
            plugin.getServer().unloadWorld(w, false);
            return;
        }
        unloaded.remove(w.getName());
    }

    @EventHandler
    public void stopAutoSave(WorldLoadEvent e) {
        setWorldSettings(e.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void noLagOnLoad(WorldInitEvent e) {
        setWorldSettings(e.getWorld());
    }

    @EventHandler
    public void stopSaving(WorldSaveEvent e) {
        setSaveSettings(e.getWorld());
    }

    private void setWorldSettings(World w) {
        if (startWithWorld(w)) {
            w.setKeepSpawnInMemory(false);
            w.setAutoSave(false);
        }
    }

    private void setSaveSettings(World w) {
        if (startWithWorld(w)) {
            w.setAutoSave(false);
        }
    }

    private boolean startWithWorld(World w) {
        return w.getName().startsWith(values.getWorldStart());
    }
}
