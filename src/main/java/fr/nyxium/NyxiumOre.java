package fr.nyxium;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NyxiumOre extends JavaPlugin implements Listener {
    private NamespacedKey chunkOreKey;
    private NamespacedKey upgradedKey;
    private NamespacedKey nyxiumItemKey;
    private NamespacedKey nyxiumBlockItemKey;
    private NamespacedKey placedNyxiumBlocksKey;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        chunkOreKey = new NamespacedKey(this, "nyxium_ore_positions");
        upgradedKey = new NamespacedKey(this, "nyxium_upgraded");
        nyxiumItemKey = new NamespacedKey(this, "nyxium_item");
        nyxiumBlockItemKey = new NamespacedKey(this, "nyxium_block_item");
        placedNyxiumBlocksKey = new NamespacedKey(this, "placed_nyxium_blocks");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NyxiumOre activé.");
    }
    
private ItemStack createNyxium() {
    ItemStack item = new ItemStack(Material.EMERALD);
    ItemMeta meta = item.getItemMeta();
    meta.setItemModel(new NamespacedKey(this, "nyxium"));
    meta.setDisplayName("§5Minerai de Nyxium");
    meta.setLore(List.of(
        "§7Un minerai extrêmement rare.",
        "§dUtilise §f/nyxium upgrade §dpour améliorer un objet."
    ));
    meta.getPersistentDataContainer().set(
        nyxiumItemKey, PersistentDataType.BYTE, (byte) 1
    );
    item.setItemMeta(meta);
    return item;
}

    private ItemStack createNyxiumBlock() {
    ItemStack item = new ItemStack(Material.DEEPSLATE_DIAMOND_ORE);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName("§5Bloc de Minerai de Nyxium");
    meta.setLore(List.of("§7Un bloc rare imprégné d'énergie violette."));
    meta.getPersistentDataContainer().set(
        nyxiumBlockItemKey, PersistentDataType.BYTE, (byte) 1
    );
    item.setItemMeta(meta);
    return item;
}

    private boolean isNyxium(ItemStack item) {
        if (item == null || item.getType() != Material.EMERALD || !item.hasItemMeta()) return false;
        Byte marker = item.getItemMeta().getPersistentDataContainer().get(nyxiumItemKey, PersistentDataType.BYTE);
        return marker != null && marker == (byte) 1;
    }

@EventHandler
public void onNyxiumBlockPlace(BlockPlaceEvent event) {
    ItemStack item = event.getItemInHand();
    if (!item.hasItemMeta()) return;

    Byte marker = item.getItemMeta()
        .getPersistentDataContainer()
        .get(nyxiumBlockItemKey, PersistentDataType.BYTE);

    if (marker == null || marker != (byte) 1) return;

    Block block = event.getBlockPlaced();
    var pdc = block.getChunk().getPersistentDataContainer();

    String data = pdc.get(placedNyxiumBlocksKey, PersistentDataType.STRING);
    List<String> positions = (data == null || data.isEmpty())
        ? new ArrayList<>()
        : new ArrayList<>(List.of(data.split(";")));

    String target = (block.getX() & 15) + ","
        + block.getY() + "," + (block.getZ() & 15);

    if (!positions.contains(target)) {
        positions.add(target);
    }

    pdc.set(
        placedNyxiumBlocksKey,
        PersistentDataType.STRING,
        String.join(";", positions)
    );
}

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        var pdc = chunk.getPersistentDataContainer();
        if (pdc.has(chunkOreKey, PersistentDataType.STRING)) return;

        World world = chunk.getWorld();
        List<String> positions = new ArrayList<>();
        int amount = 1 + random.nextInt(5);
        for (int i = 0; i < amount; i++) {
            boolean placed = false;
            for (int attempt = 0; attempt < 35 && !placed; attempt++) {
                int x = (chunk.getX() << 4) + random.nextInt(16);
                int z = (chunk.getZ() << 4) + random.nextInt(16);
                int minY = Math.max(world.getMinHeight() + 5, -55);
                int maxY = Math.min(world.getMaxHeight() - 5, 45);
                if (maxY <= minY) { minY = world.getMinHeight() + 5; maxY = Math.min(world.getMaxHeight() - 5, minY + 45); }
                int y = minY + random.nextInt(Math.max(1, maxY - minY));
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() == Material.STONE || block.getType() == Material.DEEPSLATE || block.getType() == Material.TUFF) {
                    block.setType(Material.DEEPSLATE_DIAMOND_ORE, false);
                    positions.add((x & 15) + "," + y + "," + (z & 15));
                    placed = true;
                }
            }
        }
        // An empty string also marks the chunk as processed, avoiding repeated searches.
        pdc.set(chunkOreKey, PersistentDataType.STRING, String.join(";", positions));
    }

    
@EventHandler
public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    var pdc = block.getChunk().getPersistentDataContainer();

    String target = (block.getX() & 15) + ","
        + block.getY() + "," + (block.getZ() & 15);

    // Bloc de Nyxium posé par un joueur
    String placedData = pdc.get(
        placedNyxiumBlocksKey,
        PersistentDataType.STRING
    );

    if (placedData != null && !placedData.isEmpty()) {
        List<String> placedPositions =
            new ArrayList<>(List.of(placedData.split(";")));

        if (placedPositions.remove(target)) {
            event.setDropItems(false);
            block.setType(Material.AIR, false);
            block.getWorld().dropItemNaturally(
                block.getLocation(),
                createNyxium()
            );

            pdc.set(
                placedNyxiumBlocksKey,
                PersistentDataType.STRING,
                String.join(";", placedPositions)
            );
            return;
        }
    }

    // Minerai de Nyxium généré naturellement
    String data = pdc.get(chunkOreKey, PersistentDataType.STRING);
    if (data == null || data.isEmpty()) return;

    List<String> positions =
        new ArrayList<>(List.of(data.split(";")));

    if (!positions.remove(target)) return;

    event.setDropItems(false);
    block.setType(Material.AIR, false);
    block.getWorld().dropItemNaturally(
        block.getLocation(),
        createNyxium()
    );

    pdc.set(
        chunkOreKey,
        PersistentDataType.STRING,
        String.join(";", positions)
    );
}


    
@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage("Cette commande doit être utilisée en jeu.");
        return true;
    }

if (args.length == 1 && args[0].equalsIgnoreCase("block")) {
    if (!player.isOp()) {
        player.sendMessage("§cCommande réservée aux opérateurs.");
        return true;
    }

    player.getInventory().addItem(createNyxiumBlock());
    player.sendMessage("§5Tu as reçu un Bloc de Minerai de Nyxium !");
    return true;
}

    if (args.length == 1 && args[0].equalsIgnoreCase("give")) {
        if (!player.isOp()) {
            player.sendMessage("§cCommande réservée aux opérateurs.");
            return true;
        }
        player.getInventory().addItem(createNyxium());
        player.sendMessage("§5Tu as reçu un Minerai de Nyxium !");
        return true;
    }

    if (args.length != 1 || !args[0].equalsIgnoreCase("upgrade")) {
        player.sendMessage("§dNyxium §7— commandes : §f/nyxium give §7ou §f/nyxium upgrade");
        return true;
    }

    ItemStack held = player.getInventory().getItemInMainHand();
    if (held.getType() == Material.AIR || !isUpgradeable(held.getType())) {
        player.sendMessage("§cTiens un outil ou une pièce d'armure en diamant.");
        return true;
    }

    int oreSlot = -1;
    for (int i = 0; i < player.getInventory().getSize(); i++) {
        if (isNyxium(player.getInventory().getItem(i))) {
            oreSlot = i;
            break;
        }
    }

    if (oreSlot < 0) {
        player.sendMessage("§cIl te faut un Minerai de Nyxium dans ton inventaire.");
        return true;
    }

    ItemMeta meta = held.getItemMeta();
    if (meta.getPersistentDataContainer().has(upgradedKey, PersistentDataType.BYTE)) {
        player.sendMessage("§eCet objet est déjà amélioré.");
        return true;
    }

    meta.getPersistentDataContainer().set(upgradedKey, PersistentDataType.BYTE, (byte) 1);
    List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
    lore.add("§5✦ Amélioré au Nyxium");
    lore.add("§dPouvoir : bonus de combat");
    meta.setLore(lore);
    
switch (held.getType()) {
    case DIAMOND_PICKAXE, DIAMOND_AXE -> {
        meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
    }
    case DIAMOND_SWORD -> {
        meta.addEnchant(Enchantment.SHARPNESS, 8, true);
    }
    case DIAMOND_HELMET, DIAMOND_CHESTPLATE,
         DIAMOND_LEGGINGS, DIAMOND_BOOTS -> {
        meta.addEnchant(Enchantment.PROTECTION, 8, true);
    }
    default -> {
    }
}

    held.setItemMeta(meta);

    ItemStack ore = player.getInventory().getItem(oreSlot);
    if (ore.getAmount() <= 1) {
        player.getInventory().setItem(oreSlot, null);
    } else {
        ore.setAmount(ore.getAmount() - 1);
    }

    player.sendMessage("§5✦ Ton équipement a été imprégné de Nyxium !");
    return true;
}


    private boolean isUpgradeable(Material m) {
        return switch (m) {
            case DIAMOND_SWORD, DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_HOE,
                 DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> true;
            default -> false;
        };
    }

    @EventHandler
    public void onUpgradedHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) return;
        if (!held.getItemMeta().getPersistentDataContainer().has(upgradedKey, PersistentDataType.BYTE)) return;
        event.setDamage(event.getDamage() + 3.0);
    }

    @Override
    public void onDisable() {
        getLogger().info("NyxiumOre désactivé.");
    }
}
