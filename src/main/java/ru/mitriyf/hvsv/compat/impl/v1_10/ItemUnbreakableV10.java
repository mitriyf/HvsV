package ru.mitriyf.hvsv.compat.impl.v1_10;

import org.bukkit.inventory.meta.ItemMeta;
import ru.mitriyf.hvsv.compat.abstraction.ItemUnbreakable;

public class ItemUnbreakableV10 implements ItemUnbreakable {
    @Override
    @SuppressWarnings("deprecation")
    public void setUnbreakable(ItemMeta itemMeta) {
        itemMeta.spigot().setUnbreakable(true);
    }
}
