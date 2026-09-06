package ru.mitriyf.hvsv.utils;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.mitriyf.hvsv.HvsV;
import ru.mitriyf.hvsv.compat.abstraction.ItemUnbreakable;
import ru.mitriyf.hvsv.compat.abstraction.PasteSchematic;
import ru.mitriyf.hvsv.compat.abstraction.WorldGenerator;
import ru.mitriyf.hvsv.compat.impl.v1_10.ItemUnbreakableV10;
import ru.mitriyf.hvsv.compat.impl.v1_11.ItemUnbreakableV11;
import ru.mitriyf.hvsv.compat.impl.v1_12.PasteSchematicV12;
import ru.mitriyf.hvsv.compat.impl.v1_12.WorldGeneratorV12;
import ru.mitriyf.hvsv.compat.impl.v1_13.PasteSchematicV13;
import ru.mitriyf.hvsv.compat.impl.v1_13.WorldGeneratorV13;
import ru.mitriyf.hvsv.game.Game;
import ru.mitriyf.hvsv.model.*;
import ru.mitriyf.hvsv.utils.actions.Action;
import ru.mitriyf.hvsv.utils.actions.ActionType;
import ru.mitriyf.hvsv.utils.actions.ActionUtils;
import ru.mitriyf.hvsv.utils.actions.titles.Title;
import ru.mitriyf.hvsv.utils.actions.titles.impl.Title10;
import ru.mitriyf.hvsv.utils.actions.titles.impl.Title11;
import ru.mitriyf.hvsv.utils.common.CommonUtils;
import ru.mitriyf.hvsv.utils.locales.Locale;
import ru.mitriyf.hvsv.utils.locales.impl.Locale12;
import ru.mitriyf.hvsv.utils.locales.impl.Locale13;
import ru.mitriyf.hvsv.values.Values;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Getter
public class Utils {
    private final HvsV plugin;
    private final Values values;
    private final Logger logger;
    private final CommonUtils commonUtils;
    private final ActionUtils actionUtils;
    private final ThreadLocalRandom random;
    private final Set<Integer> tasks = new HashSet<>();
    private boolean actionBar = false, bar = false, tit = false;
    private ItemUnbreakable itemUnbreakable;
    private WorldGenerator worldGenerator;
    private PasteSchematic schematic;
    private Locale locale;
    private Title title;

    public Utils(HvsV plugin) {
        this.plugin = plugin;
        values = plugin.getValues();
        logger = plugin.getLogger();
        random = plugin.getRandom();
        actionUtils = new ActionUtils(this, plugin);
        commonUtils = new CommonUtils(this, plugin);
    }

    public void setup() {
        int version = plugin.getVersion();
        if (version < 13) {
            if (version < 8) {
                tit = true;
            }
            if (version < 9) {
                bar = true;
            }
            values.setSchematicUrl("schematics.zip");
            locale = new Locale12();
            schematic = new PasteSchematicV12(plugin);
            worldGenerator = new WorldGeneratorV12();
        } else {
            values.setSchematicUrl("schems.zip");
            values.setDefaultId("13");
            locale = new Locale13();
            schematic = new PasteSchematicV13(plugin);
            worldGenerator = new WorldGeneratorV13();
        }
        if (version < 11) {
            actionBar = true;
            itemUnbreakable = new ItemUnbreakableV10();
            if (!tit) {
                title = new Title10();
            }
        } else {
            title = new Title11();
            itemUnbreakable = new ItemUnbreakableV11();
        }
    }

    public BukkitTask sendMessage(Player player, List<Action> actions, String[] search, String[] replace) {
        return sendMessage(player, actions, search, replace, true);
    }

    public BukkitTask sendMessage(CommandSender sender, Map<String, List<Action>> actions) {
        return sendMessage(sender, actions, null, null);
    }

    public BukkitTask sendMessage(CommandSender sender, List<Action> actions) {
        return sendMessage(sender, actions, null, null, sender instanceof Player);
    }

    public BukkitTask sendMessage(CommandSender sender, Map<String, List<Action>> actions, String[] search, String[] replace) {
        List<Action> actionList;
        boolean isPlayer;
        if (sender instanceof Player) {
            actionList = actions.getOrDefault(locale.player((Player) sender), actions.get(""));
            isPlayer = true;
        } else {
            actionList = actions.get("");
            isPlayer = false;
        }
        return sendMessage(sender, actionList, search, replace, isPlayer);
    }

    private BukkitTask sendMessage(CommandSender sender, List<Action> actionList, String[] search, String[] replace, boolean isPlayer) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                int delayTicks = 0;
                for (Action action : actionList) {
                    if (action.getType() == ActionType.DELAY) {
                        try {
                            delayTicks += formatInt(action.getContext());
                        } catch (Exception ignored) {
                        }
                        continue;
                    }
                    if (delayTicks > 0) {
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> sendMessage(sender, action, search, replace, isPlayer), delayTicks);
                    } else {
                        sendMessage(sender, action, search, replace, isPlayer);
                    }
                }
            }
        }.runTask(plugin);
    }

    private void sendMessage(CommandSender sender, Action action, String[] search, String[] replace, boolean isPlayer) {
        if (isPlayer) {
            sendPlayer((Player) sender, action, search, replace);
        } else {
            sendSender(sender, action, search, replace);
        }
    }

    private void sendPlayer(Player p, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext(), search, replace).replace("%player%", p.getName()).replace("%world%", p.getWorld().getName());
        if (values.isPlaceholderAPI()) {
            context = PlaceholderAPI.setPlaceholders(p, context);
        }
        switch (type) {
            case ROOM: {
                commonUtils.sendRoom(p, context);
                break;
            }
            case PLAYER: {
                actionUtils.dispatchPlayer(p, context);
                break;
            }
            case TELEPORT: {
                actionUtils.teleportPlayer(p, context);
                break;
            }
            case CONSOLE: {
                commonUtils.dispatchConsole(context);
                break;
            }
            case ACTIONBAR: {
                actionUtils.sendActionBar(p, context);
                break;
            }
            case CONNECT: {
                actionUtils.connect(p, context);
                break;
            }
            case BOSSBAR: {
                actionUtils.sendBossbar(p, context);
                break;
            }
            case BROADCAST: {
                commonUtils.broadcast(context);
                break;
            }
            case TITLE: {
                actionUtils.sendTitle(p, context);
                break;
            }
            case SOUND: {
                actionUtils.playSound(p, context);
                break;
            }
            case EFFECT: {
                actionUtils.giveEffect(p, context);
                break;
            }
            case EXPLOSION: {
                actionUtils.createExplosion(p, context);
                break;
            }
            case LOG: {
                log(context);
                break;
            }
            default: {
                sendMessage(p, context);
                break;
            }
        }
    }

    private void sendSender(CommandSender sender, Action action, String[] search, String[] replace) {
        ActionType type = action.getType();
        String context = replaceEach(action.getContext(), search, replace);
        switch (type) {
            case CONSOLE: {
                commonUtils.dispatchConsole(context);
                break;
            }
            case BROADCAST: {
                commonUtils.broadcast(context);
                break;
            }
            case LOG: {
                log(context);
                break;
            }
            case PLAYER:
            case TITLE:
            case ACTIONBAR:
            case BOSSBAR:
            case EFFECT:
            case TELEPORT:
            case SOUND:
            case CONNECT:
            case ROOM:
            case EXPLOSION:
                break;
            default:
                sendMessage(sender, context);
                break;
        }
    }

    private String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text.isEmpty() || searchList == null || replacementList == null) {
            return text;
        }
        final StringBuilder result = new StringBuilder(text);
        for (int i = 0; i < searchList.length; i++) {
            final String search = searchList[i];
            final String replacement = replacementList[i];
            int start = 0;
            while ((start = result.indexOf(search, start)) != -1) {
                result.replace(start, start + search.length(), replacement);
                start += replacement.length();
            }
        }

        return result.toString();
    }

    public void checkAxe(Player player, Game game, MapData info, Block block) {
        if (game.isAxe()) {
            Location location = block.getLocation();
            if (game.getActives().contains(location)) {
                UUID uuid = player.getUniqueId();
                MemberData memberData = game.getPlayers().get(uuid);
                if (memberData != null) {
                    if (memberData.isHunter()) {
                        return;
                    }
                    setSlots(player, values.getVictimSlots());
                    if (game.isFullSlots()) {
                        setSlots(player, info.getVictimSlots());
                    }
                    game.getAxes().add(uuid);
                    game.unSetAxe(player.getName());
                }
            }
        }
    }

    @SafeVarargs
    public final boolean checkItemIsWeapon(ItemStack stack, boolean weapon, Collection<ItemStackData>... sets) {
        if (stack == null) {
            return false;
        }
        for (Collection<ItemStackData> stackDataList : sets) {
            for (ItemStackData itemStackData : stackDataList) {
                boolean isWeapon = weapon ? itemStackData.isWeapon() : itemStackData.isExit();
                if (isWeapon && stack.isSimilar(itemStackData.getItemStack())) {
                    return true;
                }
            }
        }
        return false;
    }

    public double[] toDouble(String id) {
        String[] strings = id.split(";");
        double[] doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            try {
                doubles[i] = Double.parseDouble(strings[i]);
            } catch (Exception e) {
                values.getLogger().warning("Error parsing double. Error string: " + id + ". Error: " + e);
                doubles[i] = 0.0;
            }
        }
        return doubles;
    }

    public float[] toFloat(String id) {
        String[] strings = id.split(";");
        float[] floats = new float[strings.length];
        for (int i = 0; i < strings.length; i++) {
            try {
                floats[i] = Float.parseFloat(strings[i]);
            } catch (Exception e) {
                values.getLogger().warning("Error parsing double. Error string: " + id + ". Error: " + e);
                floats[i] = 0.0F;
            }
        }
        return floats;
    }

    public Location addOffsetLocation(OffsetData offsetData, Location location, double defaultX, double defaultZ, float yaw) {
        double[] offsetDataX = offsetData.getOffsetX();
        double offsetX = random.nextDouble(defaultX / 2 + offsetDataX[0], defaultX / 2 + offsetDataX[1]);
        double offsetY = offsetData.getOffsetY();
        double[] offsetDataZ = offsetData.getOffsetZ();
        double offsetZ = random.nextDouble(defaultZ / 2 + offsetDataZ[0], defaultZ / 2 + offsetDataZ[1]);
        float[] offsetDataPitch = offsetData.getOffsetPitch();
        float offsetPitch = (float) random.nextDouble(offsetDataPitch[0], offsetDataPitch[1]);
        location.add(offsetX, offsetY, offsetZ);
        location.setYaw(yaw);
        location.setPitch(offsetPitch);
        return location;
    }

    public void removeBlocks(World world, Location oneLocation, Location twoLocation, boolean physic) {
        Block oneBlock = oneLocation.getBlock();
        Block twoBlock = twoLocation.getBlock();
        int minX = Math.min(oneBlock.getX(), twoBlock.getX());
        int minY = Math.min(oneBlock.getY(), twoBlock.getY());
        int minZ = Math.min(oneBlock.getZ(), twoBlock.getZ());
        int maxX = Math.max(oneBlock.getX(), twoBlock.getX());
        int maxY = Math.max(oneBlock.getY(), twoBlock.getY());
        int maxZ = Math.max(oneBlock.getZ(), twoBlock.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR, physic);
                }
            }
        }
    }

    public VectorData paste(Location loc, String schematicId, boolean pasteAir) {
        try {
            return schematic.paste(loc, schematicId, pasteAir);
        } catch (Exception error) {
            logger.warning("An error occurred while inserting the diagram. Please contact the administrator.\nError: " + error);
            return new VectorData(0, 0, 0);
        }
    }

    public void checkItems(Collection<ItemStackData> valueDataCollection, List<ItemStack> list) {
        for (ItemStackData itemStackData : valueDataCollection) {
            if (itemStackData.isWeapon()) {
                list.add(itemStackData.getItemStack());
            }
        }
    }

    public void setSlots(Player player, Map<Integer, ItemStackData> slots) {
        for (Map.Entry<Integer, ItemStackData> s : slots.entrySet()) {
            player.getInventory().setItem(s.getKey(), s.getValue().getItemStack());
        }
    }

    @SuppressWarnings("deprecation")
    public void setStandHand(Set<ArmorStand> stands, ItemStack stack) {
        for (ArmorStand stand : stands) {
            stand.setItemInHand(stack);
        }
    }

    public void saveItem(Player p, String sPath, int slot, String itemName, Material material) {
        commonUtils.saveItem(p, sPath, slot, itemName, material);
    }

    public ItemStack generateItem(ConfigurationSection slot) {
        return commonUtils.generateItem(slot);
    }

    private void sendMessage(CommandSender sender, String text) {
        sender.sendMessage(formatString(text));
    }

    private void sendMessage(Player player, String text) {
        player.sendMessage(formatString(text));
    }

    public String formatString(String s) {
        return values.getColorizer().colorize(s);
    }

    public Float formatFloat(String s) {
        return Float.parseFloat(s);
    }

    public int formatInt(String s) {
        return Integer.parseInt(s);
    }

    public double formatDouble(String s) {
        return Double.parseDouble(s);
    }

    public boolean formatBoolean(String s) {
        return Boolean.parseBoolean(s);
    }

    private void log(String log) {
        logger.info(log);
    }
}
