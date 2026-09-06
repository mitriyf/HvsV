package ru.mitriyf.hvsv.hook.placeholders;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.model.MemberData;

import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {
    private final String falseString = PlaceholderAPIPlugin.booleanFalse();
    private final String trueString = PlaceholderAPIPlugin.booleanTrue();
    private final GameManager gameManager;

    public Placeholders(HvsV plugin) {
        gameManager = plugin.getGameManager();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String ind) {
        String[] args = ind.split("_");
        if (player != null && args.length >= 1) {
            UUID uuid = player.getUniqueId();
            Game game = gameManager.getGame(uuid);
            if (args[0].equalsIgnoreCase("active")) {
                if (game != null) {
                    return game.isActive() ? "started" : trueString;
                }
                return falseString;
            } else if (game != null) {
                MemberData memberData = game.getPlayers().get(uuid);
                switch (args[0].toLowerCase()) {
                    case "role": {
                        return memberData.getName();
                    }
                    case "status": {
                        return memberData.getStatus();
                    }
                    case "id": {
                        return game.getMap();
                    }
                    case "map": {
                        return game.getMapName();
                    }
                    case "online": {
                        return String.valueOf(game.getPlayers().size());
                    }
                    case "maxonline": {
                        return String.valueOf(game.getMax());
                    }
                    default: {
                        return "role, status, id, map, online, maxonline";
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "HvsV";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Mitriyf";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3";
    }
}
