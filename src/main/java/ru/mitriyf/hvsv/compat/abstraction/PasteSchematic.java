package ru.mitriyf.hvsv.compat.abstraction;

import org.bukkit.Location;
import ru.mitriyf.hvsv.model.VectorData;

import java.io.File;

public interface PasteSchematic {
    VectorData paste(Location loc, String schem, boolean pasteAir) throws Exception;

    void generate(String s, File f) throws Exception;
}
