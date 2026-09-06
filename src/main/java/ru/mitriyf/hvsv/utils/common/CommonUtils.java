package ru.mitriyf.hvsv.utils.common;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.model.MemberData;
import ru.mitriyf.hvsv.utils.Utils;
import ru.mitriyf.hvsv.values.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class CommonUtils {
    private final BukkitScheduler scheduler;
    private final HvsV plugin;
    private final Values values;
    private final Logger logger;
    private final Utils utils;

    public CommonUtils(Utils utils, HvsV plugin) {
        this.utils = utils;
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        scheduler = plugin.getServer().getScheduler();
    }

    public void sendRoom(Player player, String message) {
        String text = utils.formatString(message);
        UUID uuid = player.getUniqueId();
        Game game = plugin.getGameManager().getGame(uuid);
        if (game == null) {
            player.sendMessage(text);
            return;
        }
        for (MemberData memberData : game.getPlayers().values()) {
            memberData.getPlayer().sendMessage(text);
        }
        player.sendMessage(text);
    }

    public void broadcast(String message) {
        String text = utils.formatString(message);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(text);
        }
    }

    public void dispatchConsole(String cmd) {
        scheduler.runTaskLater(plugin, () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd), 0);
    }

    public ItemStack generateItem(ConfigurationSection slot) {
        try {
            String type = slot.getString("type");
            if (type != null && type.equalsIgnoreCase("itemstack")) {
                return slot.getItemStack("item");
            }
            slot = slot.getConfigurationSection("item");
            ItemStack item = new ItemStack(Material.valueOf(slot.getString("type")));
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            String name = slot.getString("name");
            if (name != null) {
                meta.setDisplayName(utils.formatString(name));
            }
            List<String> lore = slot.getStringList("lore");
            if (lore != null) {
                List<String> l = new ArrayList<>();
                for (String t : lore) {
                    l.add(utils.formatString(t));
                }
                meta.setLore(l);
            }
            utils.getItemUnbreakable().setUnbreakable(meta);
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            logger.warning("Error generate item " + slot.toString() + ": " + e);
            return new ItemStack(Material.AIR);
        }
    }

    @SuppressWarnings("deprecation")
    public void saveItem(Player p, String sPath, int slot, String itemName, Material material) {
        String item = sPath + "." + itemName;
        FileConfiguration slots = values.getItemSlots();
        slots.set(item + ".slot", slot);
        if (p != null) {
            slots.set(item + ".type", "itemstack");
            slots.set(item + ".item", p.getItemInHand());
        } else {
            slots.set(item + ".type", "default");
            slots.set(item + ".item.type", material.toString());
        }
        try {
            slots.save(values.getSlotsFile());
            if (p != null) {
                p.sendMessage("§aThe " + itemName + " item was created successfully!\nUse: /hvsv reload - Apply the new settings.");
            }
        } catch (Exception e) {
            logger.warning("Error item " + itemName + " save slots.yml: " + e);
        }
    }
}
