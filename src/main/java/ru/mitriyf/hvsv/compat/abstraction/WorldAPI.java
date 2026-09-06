package ru.mitriyf.hvsv.compat.abstraction;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.world.World;

import java.io.File;

public interface WorldAPI {
    EditSession getSession(World world);

    World getWorld(org.bukkit.World world);

    Clipboard getClipboard(File file) throws Exception;
}
