package ru.mitriyf.hvsv.utils.locales.impl;

import org.bukkit.entity.Player;
import ru.mitriyf.hvsv.utils.locales.Locale;

public class Locale13 implements Locale {
    @Override
    public String player(Player p) {
        return p.getLocale().toLowerCase();
    }
}
