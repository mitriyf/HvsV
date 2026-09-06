package ru.mitriyf.hvsv.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class MemberData {
    private final Player player;
    private String locale, status, name;
    private boolean hunter, spectator;

    public MemberData(Player player) {
        this.player = player;
    }
}
