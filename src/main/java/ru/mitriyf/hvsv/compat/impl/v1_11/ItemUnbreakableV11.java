package ru.mitriyf.hvsv.compat.impl.v1_11;

import org.bukkit.inventory.meta.ItemMeta;
import ru.mitriyf.hvsv.compat.abstraction.ItemUnbreakable;

public class ItemUnbreakableV11 implements ItemUnbreakable {
    @Override
    public void setUnbreakable(ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }
}
