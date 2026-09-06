package ru.mitriyf.hvsv.compat.impl.v1_13;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.compat.abstraction.PasteSchematic;
import ru.mitriyf.hvsv.compat.abstraction.WorldAPI;
import ru.mitriyf.hvsv.compat.impl.v1_15.WorldAPIV15;
import ru.mitriyf.hvsv.model.VectorData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PasteSchematicV13 implements PasteSchematic {
    private final Map<String, Clipboard> clipboards = new HashMap<>();
    private final WorldAPI worldAPI;

    public PasteSchematicV13(HvsV plugin) {
        if (plugin.getVersion() < 15) {
            worldAPI = new WorldAPIV13();
        } else {
            worldAPI = new WorldAPIV15();
        }
    }

    @Override
    public VectorData paste(Location loc, String schem, boolean pasteAir) {
        World world = worldAPI.getWorld(loc.getWorld());
        EditSession editSession = worldAPI.getSession(world);
        Clipboard clipboard = clipboards.get(schem);
        clipboard.paste(editSession, BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()), pasteAir, null);
        Operations.complete(editSession.commit());
        editSession.close();
        BlockVector3 vector3 = clipboard.getDimensions();
        return new VectorData(vector3.getX(), vector3.getY(), vector3.getZ());
    }

    @Override
    public void generate(String schematicName, File file) throws Exception {
        Clipboard clipboard = worldAPI.getClipboard(file);
        clipboards.put(schematicName, clipboard);
    }
}
