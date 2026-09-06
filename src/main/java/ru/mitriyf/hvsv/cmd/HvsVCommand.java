package ru.mitriyf.hvsv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.cmd.subcommand.AdminEditorSubCommand;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.manager.GameManager;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.values.Values;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HvsVCommand implements CommandExecutor {
    private final AdminEditorSubCommand adminEditorSubCommand;
    private final GameManager gameManager;
    private final Values values;
    private final Utils utils;

    public HvsVCommand(HvsV plugin) {
        utils = plugin.getUtils();
        values = plugin.getValues();
        gameManager = plugin.getGameManager();
        adminEditorSubCommand = new AdminEditorSubCommand(plugin);
    }

    private void sendMessage(CommandSender sender, Map<String, List<Action>> message) {
        utils.sendMessage(sender, message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        boolean permission = sender.hasPermission("hvsv.help");
        if (!permission || args.length == 0 || args.length >= 8 || args[0].equalsIgnoreCase("help")) {
            if (permission) {
                sendMessage(sender, values.getHelp());
            } else {
                sendMessage(sender, values.getNoperm());
            }
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "join": {
                join(sender, args);
                return false;
            }
            case "exit": {
                exit(sender);
                return false;
            }
            case "admin": {
                adminEditorSubCommand.checkAdminSubCommand(sender, args);
                return false;
            }
            case "status": {
                status(sender);
                return false;
            }
            case "reload": {
                reload(sender);
                return false;
            }
            default: {
                sendMessage(sender, values.getHelp());
            }
        }
        return false;
    }

    private void join(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("hvsv.join")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        Player p = (Player) sender;
        if (args.length == 2) {
            gameManager.join(p, args[1].toLowerCase());
            return;
        }
        gameManager.join(p, null);
    }

    private void exit(CommandSender sender) {
        if (!(sender instanceof Player) || !sender.hasPermission("hvsv.join")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();
        BukkitTask task = gameManager.getTasks().get(uuid);
        if (task != null) {
            task.cancel();
            gameManager.getTasks().remove(uuid);
            gameManager.getWaiters().remove(uuid);
            utils.sendMessage(p, values.getExit());
            return;
        }
        Game game = gameManager.getGame(p.getUniqueId());
        if (game != null) {
            game.kickPlayer(p, false, false);
        } else {
            utils.sendMessage(p, values.getNoExit());
        }
    }

    private void status(CommandSender sender) {
        if (!sender.hasPermission("hvsv.status")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        sender.sendMessage("§aPlugin status:\n\n");
        sender.sendMessage("§fRooms: §a" + gameManager.getRooms().size());
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("hvsv.reload")) {
            sendMessage(sender, values.getNoperm());
            return;
        }
        values.setup(false);
        sender.sendMessage("§aSuccessfully!");
    }
}
