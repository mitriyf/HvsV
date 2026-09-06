package ru.mitriyf.hvsv.compat.impl.v1_12;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.compat.abstraction.PasteSchematic;
import ru.mitriyf.hvsv.compat.abstraction.WorldAPI;
import ru.mitriyf.hvsv.compat.impl.v1_13.WorldAPIV13;
import ru.mitriyf.hvsv.model.VectorData;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class PasteSchematicV12 implements PasteSchematic {
    private final Map<String, Schematic> clipboards = new HashMap<>();
    private final WorldAPI worldAPI;
    private final File schematicsDir;
    private Class<?> vClass, worldData;
    private Method toBlockPoint, findFile, setSourceMaskMethod;
    private Constructor<?> createClipboard, createRegion, copyForward, maskConstructor;
    private Object schematicFormat;

    public PasteSchematicV12(HvsV plugin) {
        schematicsDir = new File(plugin.getValues().getDataFolder(), plugin.getValues().getSchematicsDir());
        worldAPI = new WorldAPIV13();
        try {
            Class<?> clipboardFormats = Class.forName("com.sk89q.worldedit.extent.clipboard.ClipboardFormats");
            worldData = Class.forName("com.sk89q.worldedit.world.registry.WorldData");
            maskConstructor = ExistingBlockMask.class.getConstructor(Extent.class);
            setSourceMaskMethod = ForwardExtentCopy.class.getMethod("setSourceMask", Mask.class);
            vClass = Class.forName("com.sk89q.worldedit.Vector");
            copyForward = ForwardExtentCopy.class.getConstructor(Extent.class, Region.class, Extent.class, vClass);
            toBlockPoint = vClass.getMethod("toBlockPoint", double.class, double.class, double.class);
            findFile = clipboardFormats.getMethod("findByFile", File.class);
            createRegion = CuboidRegion.class.getConstructor(World.class, vClass, vClass);
            createClipboard = BlockArrayClipboard.class.getConstructor(Region.class);
            schematicFormat = ClipboardFormat.class.getField("SCHEMATIC").get(null);
        } catch (Exception e) {
            plugin.getLogger().warning("Error retrieving classes for schematics. Error: " + e);
        }
    }

    @Override
    public VectorData paste(Location loc, String schem, boolean pasteAir) throws Exception {
        Object vector = toBlockPoint.invoke(null, loc.getX(), loc.getY(), loc.getZ());
        Object world = worldAPI.getWorld(loc.getWorld());
        Schematic sch = clipboards.get(schem);
        Method paste = sch.getClass().getMethod("paste", World.class, vClass, boolean.class, boolean.class, Transform.class);
        paste.invoke(sch, world, vector, false, pasteAir, null);
        Clipboard clipboard = sch.getClipboard();
        Vector vector3 = (Vector) clipboard.getClass().getMethod("getDimensions").invoke(clipboard);
        return new VectorData(vector3.getX(), vector3.getY(), vector3.getZ());
    }

    private Object createRegion(World world, Location pose1, Location pose2) throws Exception {
        Location min;
        Location max;
        if (pose1.getY() < pose2.getY()) {
            min = pose1;
            max = pose2;
        } else {
            min = pose2;
            max = pose1;
        }
        Object pose1Vector = toBlockPoint.invoke(null, min.getX(), min.getY(), min.getZ());
        Object pose2Vector = toBlockPoint.invoke(null, max.getX(), max.getY(), max.getZ());
        return createRegion.newInstance(world, pose1Vector, pose2Vector);
    }

    @Override
    public void generate(String s, File f) throws Exception {
        Object format = findFile.invoke(null, f);
        Method load = format.getClass().getMethod("load", File.class);
        Schematic sch = (Schematic) load.invoke(format, f);
        clipboards.put(s, sch);
    }
}
