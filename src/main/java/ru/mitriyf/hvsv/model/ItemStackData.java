package ru.mitriyf.hvsv.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ItemStackData {
    private final ItemStack itemStack;
    @Setter
    private boolean exit, weapon;

    public ItemStackData(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
