package ru.mitriyf.hvsv.compat.impl.v1_15;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;
import ru.mitriyf.hvsv.compat.abstraction.WorldAPI;

import java.io.File;

public class WorldAPIV15 implements WorldAPI {
    @Override
    public EditSession getSession(World world) {
        return WorldEdit.getInstance().newEditSession(world);
    }

    @Override
    public World getWorld(org.bukkit.World world) {
        return FaweAPI.getWorld(world.getName());
    }

    @Override
    public Clipboard getClipboard(File file) throws Exception {
        return FaweAPI.load(file);
    }
}
