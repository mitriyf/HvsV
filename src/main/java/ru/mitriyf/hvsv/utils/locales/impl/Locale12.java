package ru.mitriyf.hvsv.utils.locales.impl;

import org.bukkit.entity.Player;
import ru.mitriyf.hvsv.utils.locales.Locale;

@SuppressWarnings("deprecation")
public class Locale12 implements Locale {
    @Override
    public String player(Player p) {
        return p.spigot().getLocale().toLowerCase();
    }
}
