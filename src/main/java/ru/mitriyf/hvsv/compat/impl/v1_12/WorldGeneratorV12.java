package ru.mitriyf.hvsv.compat.impl.v1_12;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import ru.mitriyf.hvsv.compat.abstraction.WorldGenerator;

public class WorldGeneratorV12 implements WorldGenerator {
    @Override
    public World generateWorld(String name) {
        return new WorldCreator(name).type(WorldType.FLAT).generatorSettings("2;0;1;").createWorld();
    }
}
