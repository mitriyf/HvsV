package ru.mitriyf.hvsv.hook;

import lombok.Getter;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.hook.placeholders.Placeholders;
import ru.mitriyf.hvsv.values.Values;

public class Supports {
    private final HvsV plugin;
    private final Values values;
    @Getter
    private Placeholders placeholders;

    public Supports(HvsV plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
    }

    public void register() {
        if (values.isPlaceholderAPI()) {
            placeholders = new Placeholders(plugin);
            placeholders.register();
        }
    }

    public void unregister() {
        if (placeholders != null) {
            placeholders.unregister();
            placeholders = null;
        }
    }
}
