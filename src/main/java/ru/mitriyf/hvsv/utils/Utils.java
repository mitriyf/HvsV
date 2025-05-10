package ru.mitriyf.hvsv.utils;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.values.Values;
import ru.mitriyf.hvsv.game.Game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
    private final HvsV plugin;
    private final Values values;
    private final ThreadLocalRandom rnd = ThreadLocalRandom.current();
    public Utils(HvsV plugin) {
        this.plugin = plugin;
        this.values = plugin.getValues();
    }
    // Minecraft
    private final Pattern PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public String hex(String message) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = PATTERN.matcher(message);
        if (plugin.getVersion_mode() >= 16) {
            while (matcher.find()) {
                String color = matcher.group(1);
                StringBuilder replacement = new StringBuilder("§x");
                for (char c : color.toCharArray())
                    replacement.append('§').append(c);
                matcher.appendReplacement(buffer, replacement.toString());
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString().replace('&', '§');
    }
    public void generateItems() {
        values.setExitI(generateExit());
        values.setAxeI(createI(Material.valueOf(values.getAxe())));
        values.setSwordI(createI(Material.valueOf(values.getSword())));
        values.setHelmetI(createI(Material.valueOf(values.getHelmet())));
        values.setAirI(new ItemStack(Material.AIR));
    }
    private ItemStack generateExit() {
        ItemStack stack = new ItemStack(Material.valueOf(values.getExitMaterial()));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(hex(values.getExitName()));
        if (!values.getExitLore().isEmpty()) meta.setLore(values.getExitLore());
        stack.setItemMeta(meta);
        return stack;
    }
    private ItemStack createI(Material m) {
        ItemStack stack = new ItemStack(m);
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 9999, true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        return stack;
    }
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(hex(message));
    }
    public void sendMessage(Player p, String text) {
        if (text == null) return;
        String formatted = text
                .replace("%player%", p.getName())
                .replace("%max_players%", String.valueOf(values.getMax_players()));
        if (formatted.contains("%rnd_player%")) {
            List<Player> playersList = new ArrayList<>(Bukkit.getOnlinePlayers());
            String name = playersList.get(ThreadLocalRandom.current().nextInt(playersList.size())).getName();
            formatted = formatted.replace("%rnd_player%", name);
        }
        formatted = PlaceholderAPI.setPlaceholders(p, formatted);
        String lowerCase = formatted.toLowerCase();
        if (!lowerCase.startsWith("[") || lowerCase.startsWith("[message] ")) {
            text = hex(formatted.replace("[message] ", ""));
            p.sendMessage(text);
        }
        else if (lowerCase.startsWith("[player] ")) p.performCommand(hex(formatted.replace("[player] ", "")));
        else if (lowerCase.startsWith("[console] ")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hex(formatted.replace("[console] ", "")));
        else if (lowerCase.startsWith("[broadcast] ")) for (Player pls : Bukkit.getOnlinePlayers()) pls.sendMessage(hex(formatted.replace("[broadcast] ", "")));
        else if (lowerCase.startsWith("[sound] ")) playSound(p, formatted);
        else if (lowerCase.startsWith("[title] ")) {
            String[] title = formatted.replace("[title] ", "").split(";");
            if (title.length == 2) p.sendTitle(hex(title[0]), hex(title[1]));
            else plugin.getLogger().warning("An error occurred when calling title from the configuration. (title;title)");
        }
        else p.sendMessage(hex(text));
    }
    private void playSound(Player p, String formatted) {
        formatted = formatted.replace("[sound] ", "");
        Sound sound;
        int volume = 1;
        int pitch = 1;
        try {
            if (formatted.contains(";")) {
                String[] split = formatted.split(";");
                sound = Sound.valueOf(split[0]);
                volume = Integer.parseInt(split[1]);
                pitch = Integer.parseInt(split[2]);
            }
            else sound = Sound.valueOf(formatted);
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
        catch (Exception e) {
            plugin.getLogger().warning("An error occurred when calling sound from the configuration");
            e.printStackTrace();
        }
    }
    public void join(Player p) {
        if (values.getPlayers().containsKey(p.getUniqueId())) return;
        if (values.getRooms().isEmpty()) {
            sendMessage(p, values.getConnect().replace("%room%", "room-1"));
            values.getRooms().put("room-1", new Game(plugin, "room-1", p));
            return;
        }
        for (Game game : values.getRooms().values()) {
            if (!game.isActive()) {
                sendMessage(p, values.getConnect().replace("%room%", game.getName()));
                game.addPlayer(p);
                return;
            }
        }
        generateRoom(p);
    }
    private void generateRoom(Player p) {
        String name = "room-" + rnd.nextInt(999);
        if (!values.getRooms().containsKey(name)) {
            sendMessage(p, values.getConnect().replace("%room%", name));
            values.getRooms().put(name, new Game(plugin, name, p));
        }
        else generateRoom(p);
    }

    public void joinRoom(Player p, String room) {
        if (values.getPlayers().containsKey(p.getUniqueId())) return;
        if (values.getRooms().isEmpty()) {
            sendMessage(p, values.getNotfound());
            return;
        }
        if (values.getRooms().containsKey(room)) {
            Game game = values.getRooms().get(room);
            if (!game.isActive()) {
                sendMessage(p, values.getConnect().replace("%room%", game.getName()));
                values.getRooms().get(room).addPlayer(p);
            }
            else sendMessage(p, values.getStarted());
        }
        else sendMessage(p, values.getNotfound());
    }
    public Vector paste(Location loc, Set<EditSession> schems, String category, String schematic) {
        File schem = new File(plugin.getDataFolder(), category + "/" + schematic);
        if (!schem.exists()) {
            plugin.getLogger().warning("File " + schematic + ".schematic not found.");
            return null;
        }
        try {
            Vector lc = Vector.toBlockPoint(loc.getX(), loc.getY(), loc.getZ());
            Schematic sch = ClipboardFormats.findByFile(schem).load(schem);
            EditSession session = sch.paste(new BukkitWorld(loc.getWorld()), lc, true, false, null);
            schems.add(session);
            return sch.getClipboard().getDimensions();
        } catch (IOException error) {
            error.printStackTrace();
            return null;
        }
    }
    // Другое
    public void unpack(String zip, String dir) throws IOException {
        Path destDirPath = Paths.get(dir);
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(zip)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = destDirPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }
}
