package ru.mitriyf.hvsv.compat.impl.v1_13;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import ru.mitriyf.hvsv.compat.abstraction.WorldGenerator;
import ru.mitriyf.hvsv.generator.EmptyGenerator;

public class WorldGeneratorV13 implements WorldGenerator {
    @Override
    public World generateWorld(String name) {
        return new WorldCreator(name).generator(new EmptyGenerator()).createWorld();
    }
}
