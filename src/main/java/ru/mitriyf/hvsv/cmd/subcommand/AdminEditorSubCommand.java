package ru.mitriyf.hvsv.cmd.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.cmd.subcommand.admin.ItemEditorSubCommand;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.Values;

public class AdminEditorSubCommand {
    private final ItemEditorSubCommand itemEditorSubCommand;
    private final GameManager gameManager;
    private final Values values;
    private final Utils utils;
    private final HvsV plugin;

    public AdminEditorSubCommand(HvsV plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
        gameManager = plugin.getGameManager();
        itemEditorSubCommand = new ItemEditorSubCommand(plugin);
    }

    public void checkAdminSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hvsv.admin")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        }
        if (args.length > 1 && args.length < 8) {
            switch (args[1].toLowerCase()) {
                case "add": {
                    addGame(sender, args);
                    return;
                }
                case "item": {
                    itemEditorSubCommand.checkItemSubCommand(sender, args);
                    return;
                }
                case "kick": {
                    closeGame(sender, args);
                    return;
                }
                case "locale": {
                    getLocale(sender);
                    return;
                }
            }
        }
        sendAdminHelp(sender);
    }

    private void addGame(CommandSender sender, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            sender.sendMessage("§cThis player is not found.\n§c/hvsv admin add playerName Map");
            return;
        }
        Player p = plugin.getServer().getPlayer(args[2]);
        gameManager.join(p, args.length == 4 ? args[3].toLowerCase() : null);
        sender.sendMessage("§aConnection attempt has been sent.");
    }

    private void getLocale(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        sender.sendMessage("§aYour language code:\n" + utils.getLocale().player((Player) sender));
    }

    private void closeGame(CommandSender sender, String[] args) {
        Game game = getGame(sender, args);
        if (game != null) {
            game.close(true, false);
            sender.sendMessage("§aSuccessfully!");
        }
    }

    private Game getGame(CommandSender sender, String[] args) {
        if (args.length < 3 || plugin.getServer().getPlayer(args[2]) == null) {
            sender.sendMessage("§cThis player is not found/The command was executed incorrectly.");
            return null;
        }
        Player player = plugin.getServer().getPlayer(args[2]);
        Game game = gameManager.getGame(player.getUniqueId());
        if (game == null) {
            sender.sendMessage("§cThe player is not in the game.");
            return null;
        }
        return game;
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§aHvsV Admin Help:\n");
        sender.sendMessage("§a/hvsv admin add playerName Map §f- Add a player to a specific game.");
        sender.sendMessage("§a/hvsv admin add playerName §f- Add a player to a random game.");
        sender.sendMessage("§a/hvsv admin item §f- Get a Item Help.");
        sender.sendMessage("§a/hvsv admin kick playerName §f- Kick the player out of the game.");
        sender.sendMessage("§a/hvsv admin locale §f- Get the client's language code.");
    }
}
