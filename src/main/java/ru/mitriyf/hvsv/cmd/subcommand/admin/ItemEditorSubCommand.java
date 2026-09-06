package ru.mitriyf.hvsv.cmd.subcommand.admin;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.Values;

import java.util.Map;
import java.util.Set;

public class ItemEditorSubCommand {
    private final Values values;
    private final Utils utils;
    private final HvsV plugin;

    public ItemEditorSubCommand(HvsV plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        utils = plugin.getUtils();
    }

    public void checkItemSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hvsv.item")) {
            utils.sendMessage(sender, values.getNoperm());
            return;
        }
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "add": {
                    addItem(sender, args);
                    return;
                }
                case "list": {
                    listItems(sender, args);
                    return;
                }
                case "info": {
                    infoItem(sender, args);
                    return;
                }
                case "remove": {
                    removeItem(sender, args);
                    return;
                }
                default: {
                    sendItemHelp(sender);
                    return;
                }
            }
        }
        sendItemHelp(sender);
    }

    @SuppressWarnings("deprecation")
    private void addItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cYou console!");
            return;
        }
        Player player = (Player) sender;
        if (args.length == 7) {
            if (player.getItemInHand().getType() == Material.AIR) {
                player.sendMessage("§cYour hand is empty. The item is accepted.");
            }
            boolean args3Default = args[3].equals("default");
            if (!args3Default && !values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[3])) {
                player.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
                return;
            }
            String role = args[4].toLowerCase();
            if (!role.equals("hunter") && !role.equals("victim") && !role.equals("player")) {
                player.sendMessage("§cEnter hunter, victim, or player");
                return;
            }
            String sPath = (args3Default ? "default." : "schematics." + args[3]) + "." + role;
            int slot;
            try {
                slot = Integer.parseInt(args[5]);
                if (slot < 0) {
                    player.sendMessage("§cInsert a number > 0!");
                    return;
                }
            } catch (Exception e) {
                player.sendMessage("§cInsert a number, not a string");
                return;
            }
            String itemName = args[6];
            Set<String> items = values.getItemSlots().getConfigurationSection(sPath).getKeys(false);
            if (items.contains(itemName)) {
                player.sendMessage("§cThe item name is already taken. Change the name or delete that item.");
                return;
            }
            for (String i : items) {
                if (slot == values.getItemSlots().getInt(sPath + "." + i + ".slot")) {
                    player.sendMessage("§cThe item slot is already taken. Change the slot or delete item " + i);
                    return;
                }
            }
            utils.saveItem(player, sPath, slot, itemName, null);
        } else {
            player.sendMessage("§aUse: /hvsv admin item add default/schematicName player/victim/hunter slot itemName §f- Add the item in your hand to the selected schematic.");
        }
    }

    private void listItems(CommandSender s, String[] args) {
        if (args.length == 5) {
            boolean args3Default = args[3].equals("default");
            if (args3Default || values.getItemSlots().createSection("schematics").getKeys(false).contains(args[3])) {
                String role = args[4].toLowerCase();
                if (!role.equals("hunter") && !role.equals("victim") && !role.equals("player")) {
                    s.sendMessage("§cEnter hunter, victim, or player");
                    return;
                }
                String id = args3Default ? values.getItemSlots().getConfigurationSection("default." + role).getKeys(false).toString() : values.getItemSlots().getConfigurationSection("schematics." + args[3] + "." + role).getKeys(false).toString();
                s.sendMessage("§aList items " + args[3] + "." + role + " schematic:\n§f" + id);
                return;
            }
        }
        s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
    }

    private void infoItem(CommandSender s, String[] args) {
        if (args.length == 6) {
            boolean args3Default = args[3].equals("default");
            if (args3Default || values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[3])) {
                String schematicName = args[3], bigRole = args[4], itemName = args[5];
                ConfigurationSection schematics = getSchematicsSection(s, schematicName, bigRole, args3Default);
                if (schematics == null) {
                    return;
                }
                ConfigurationSection item = schematics.getConfigurationSection(itemName);
                if (item != null) {
                    s.sendMessage("§aItem " + itemName + ":\n");
                    for (Map.Entry<String, Object> section : item.getValues(true).entrySet()) {
                        s.sendMessage("§f" + section.getKey() + ": " + section.getValue());
                    }
                    return;
                }
                s.sendMessage("§aList items " + schematicName + "." + bigRole + " schematic:\n§f" + schematics.getKeys(false).toString());
                return;
            }
            s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
            return;
        }
        s.sendMessage("§a/hvsv admin item info default/schematicName player/victim/hunter itemName §f- Get the item from the selected schematic.");
    }

    private void removeItem(CommandSender s, String[] args) {
        if (args.length == 6) {
            boolean args3Default = args[3].equals("default");
            if (args3Default || values.getItemSlots().getConfigurationSection("schematics").getKeys(false).contains(args[3])) {
                String schematicName = args[3], bigRole = args[4], itemName = args[5];
                ConfigurationSection schematics = getSchematicsSection(s, schematicName, bigRole, args3Default);
                if (schematics == null) {
                    return;
                }
                ConfigurationSection item = schematics.getConfigurationSection(itemName);
                if (item != null) {
                    FileConfiguration slots = values.getItemSlots();
                    slots.set(item.getCurrentPath(), null);
                    try {
                        slots.save(values.getSlotsFile());
                        s.sendMessage("§aThe item " + itemName + " has been successfully deleted.\nUse: /hvsv reload - Apply the new settings.");
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error item " + itemName + " save slots.yml: " + e);
                    }
                    return;
                }
                s.sendMessage("§aList items " + schematicName + "." + bigRole + " schematic:\n§f" + schematics.getKeys(false).toString());
                return;
            }
            s.sendMessage("§aList schematics:\n§f" + values.getSchematics().keySet());
            return;
        }
        s.sendMessage("§a/hvsv admin item remove default/schematicName player/victim/hunter itemName §f- Remove an item from the selected schematic.");
    }

    private ConfigurationSection getSchematicsSection(CommandSender sender, String schematicName, String bigRole, boolean args3Default) {
        String role = bigRole.toLowerCase();
        if (!role.equals("hunter") && !role.equals("victim") && !role.equals("player")) {
            sender.sendMessage("§cEnter hunter, victim, or player");
            return null;
        }
        return args3Default ? values.getItemSlots().getConfigurationSection("default." + role) : values.getItemSlots().getConfigurationSection("schematics." + schematicName + "." + role);
    }

    private void sendItemHelp(CommandSender s) {
        s.sendMessage("§aHvsV Item Help:\n");
        s.sendMessage("§a/hvsv admin item add default/schematicName player/victim/hunter slot itemName §f- Add the item in your hand to the selected schematic.");
        s.sendMessage("§a/hvsv admin item list §f- Get a list of schematics.");
        s.sendMessage("§a/hvsv admin item list default/schematicName player/victim/hunter §f- Get a list of items in the selected schematic.");
        s.sendMessage("§a/hvsv admin item info default/schematicName player/victim/hunter itemName §f- Get the item from the selected schematic.");
        s.sendMessage("§a/hvsv admin item remove default/schematicName player/victim/hunter itemName §f- Remove an item from the selected schematic.");
    }
}
