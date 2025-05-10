package ru.mitriyf.hvsv.supports;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;
import ru.mitriyf.hvsv.game.Game;

public class Placeholder extends PlaceholderExpansion {
    private final Values values;

    public Placeholder(HvsV plugin) {
        values = plugin.getValues();
    }
    @Override
    public String onPlaceholderRequest(Player p, @NotNull String ind) {
        String[] args = ind.split("_");
        if (args.length >= 1) {
            String tru = PlaceholderAPIPlugin.booleanTrue();
            String fals = PlaceholderAPIPlugin.booleanFalse();
            String gameId = "null";
            Game game = null;
            if (values.getPlayers().containsKey(p.getUniqueId())) {
                gameId = values.getPlayers().get(p.getUniqueId()).getGame();
                game = values.getRooms().get(gameId);
            }
            if (args[0].equalsIgnoreCase("active")) {
                if (!gameId.equals("null")) {
                    if (!values.getRooms().get(gameId).isActive()) return tru;
                    else return "started";
                }
                return fals;
            }
            else if (game != null) {
                if (args[0].equalsIgnoreCase("status")) return game.getStatus().replace(".0", "");
                else if (args[0].equalsIgnoreCase("map")) return game.getMap();
                else if (args[0].equalsIgnoreCase("role")) {
                    if (game.getHunters().contains(p.getUniqueId())) return values.getHunter();
                    else return values.getVictim();
                } else if (args[0].equalsIgnoreCase("online")) return String.valueOf(game.getPlayers().size());
            }
        }
        return null;
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
        return "1.0";
    }
}
