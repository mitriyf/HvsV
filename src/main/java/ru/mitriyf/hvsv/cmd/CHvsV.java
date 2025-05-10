package ru.mitriyf.hvsv.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;
import ru.mitriyf.hvsv.utils.Utils;

import java.util.List;

public class CHvsV implements CommandExecutor {
    private final HvsV plugin;
    private final Values values;
    private final Utils utils;
    public CHvsV(HvsV plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
        this.utils = plugin.getUtils();
    }
    private void sendMessage(CommandSender sender, String message) {
        utils.sendMessage(sender, message);
    }
    private void sendMessage(CommandSender sender, List<String> message) {
        for (String msg : message) utils.sendMessage(sender, msg);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0 || args.length >= 3 || args[0].equalsIgnoreCase("help")) {
            if (sender.hasPermission("hvsv.help")) sendMessage(sender, values.getHelp());
            else sendMessage(sender, values.getNoperm());
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "join": {
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "&cYou console!");
                    return false;
                }
                Player p = (Player) sender;
                if (args.length == 2) {
                    utils.joinRoom(p, args[1]);
                    return false;
                }
                utils.join(p);
                return false;
            }
            case "status": {
                if (!sender.hasPermission("hvsv.status")) {
                    sendMessage(sender, values.getNoperm());
                    return false;
                }
                sendMessage(sender, "&aСтатус плагина:\n\n&fКомнат: &a" + values.getRooms().size() + "\nСостояние: &aАктивный\n");
                return false;
            }
            case "reload": {
                if (!sender.hasPermission("hvsv.reload")) {
                    sendMessage(sender, values.getNoperm());
                    return false;
                }
                plugin.reloadConfig();
                values.setup();
                utils.generateItems();
                sendMessage(sender, "Успешно!");
            }
        }
        return false;
    }
}
