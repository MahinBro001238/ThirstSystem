package com.mahin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import java.util.List;
public class Plugin extends JavaPlugin implements Listener {
    NamespacedKey ThirstKey;
    NamespacedKey RecievingThirstDamageKey;
    NamespacedKey DiedOfThirstKey;
    NamespacedKey FlaskKey;
    ItemStack EmptyWaterFlask;
    static class ThirstPlaceholder extends PlaceholderExpansion {
        private final NamespacedKey ThirstKey;
        ThirstPlaceholder(NamespacedKey ThirstKey) {
            this.ThirstKey = ThirstKey;
        }
        @Override
        public String getIdentifier() {
            return "thirst";
        }
        @Override
        public String getAuthor() {
            return "mahin";
        }
        @Override
        public String getVersion() {
            return "1.0";
        }
        @Override
        public boolean persist() {
            return true;
        }
        @Override
        public String onRequest(OfflinePlayer offlinePlayer, String params) {
            return String.valueOf(offlinePlayer.getPersistentDataContainer().get(ThirstKey, PersistentDataType.INTEGER));
        }
    }
    private ItemStack WaterFlask(int water_level) {
        ItemStack WaterFlask = new ItemStack(Material.POTION);
        PotionMeta PotionMeta = (PotionMeta) WaterFlask.getItemMeta();
        PotionMeta.setBasePotionType(PotionType.WATER);
        PotionMeta.displayName(Component.text("Water Flask"));
        PotionMeta.setEnchantmentGlintOverride(true);
        PotionMeta.setMaxStackSize(1);
        PotionMeta.lore(List.of(Component.text(water_level + "/100")));
        PotionMeta.getPersistentDataContainer().set(FlaskKey, PersistentDataType.BOOLEAN, true);
        CustomModelDataComponent CMD = PotionMeta.getCustomModelDataComponent();
        CMD.setStrings(List.of("water_flask"));
        PotionMeta.setCustomModelDataComponent(CMD);
        WaterFlask.setItemMeta(PotionMeta);
        return WaterFlask;
    }
    private void DealDamage(Player player) {
        double absorption = player.getAbsorptionAmount();
        if (absorption > 0) {
            double remaining = absorption - 2;
            if (remaining < 0) {
                player.setAbsorptionAmount(0);
                player.setHealth(Math.max(0, player.getHealth() + remaining));
            }
            else {
                player.setAbsorptionAmount(remaining);
            }
        }
        else {
            player.setHealth(Math.max(0, player.getHealth() - 2));
        }
    }
    private void DealThirstDamage(Player player) {
        PersistentDataContainer PDC = player.getPersistentDataContainer();
        if (!PDC.has(RecievingThirstDamageKey)) {
            PDC.set(RecievingThirstDamageKey, PersistentDataType.BOOLEAN, true);
            BukkitTask[] task = new BukkitTask[1];
            task[0] = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        PersistentDataContainer PDC = player.getPersistentDataContainer();
                        if (PDC.get(ThirstKey, PersistentDataType.INTEGER) == 0) {
                            if (player.getHealth() + player.getAbsorptionAmount() <= 2) {
                                PDC.set(DiedOfThirstKey, PersistentDataType.BOOLEAN, true);
                                PDC.remove(RecievingThirstDamageKey);
                                DealDamage(player);
                                task[0].cancel();
                            }
                            else {
                                DealDamage(player);
                            }
                        }
                        else {
                            PDC.remove(RecievingThirstDamageKey);
                            task[0].cancel();
                        }
                    }
                    else {
                        task[0].cancel();
                    }
                }
            }, 40L, 40L);
        }
    }
    @Override
    public void onEnable() {
        ThirstKey = new NamespacedKey(this, "thirst");
        DiedOfThirstKey = new NamespacedKey(this, "died_of_thirst");
        RecievingThirstDamageKey = new NamespacedKey(this, "recieving_thirst_damage");
        FlaskKey = new NamespacedKey(this, "flask");
        EmptyWaterFlask = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta EmptyWaterFlaskMeta = EmptyWaterFlask.getItemMeta();
        EmptyWaterFlaskMeta.displayName(Component.text("Empty Water Flask"));
        EmptyWaterFlaskMeta.setEnchantmentGlintOverride(true);
        EmptyWaterFlaskMeta.setMaxStackSize(1);
        EmptyWaterFlaskMeta.getPersistentDataContainer().set(FlaskKey, PersistentDataType.BOOLEAN, true);
        CustomModelDataComponent CMD = EmptyWaterFlaskMeta.getCustomModelDataComponent();
        CMD.setStrings(List.of("empty_water_flask"));
        EmptyWaterFlaskMeta.setCustomModelDataComponent(CMD);
        EmptyWaterFlask.setItemMeta(EmptyWaterFlaskMeta);
        ShapedRecipe EmptyWaterFlaskRecipe = new ShapedRecipe(new NamespacedKey(this, "empty_water_flask_recipe"), EmptyWaterFlask);
        EmptyWaterFlaskRecipe.shape("GGG", "GGG", "GGG");
        EmptyWaterFlaskRecipe.setIngredient('G', Material.GLASS_BOTTLE);
        Bukkit.addRecipe(EmptyWaterFlaskRecipe);
        new ThirstPlaceholder(ThirstKey).register();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Thirst System has been enabled");
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer PDC = player.getPersistentDataContainer();
        if (!PDC.has(ThirstKey)) {
            PDC.set(ThirstKey, PersistentDataType.INTEGER, 20);
        }
        if (PDC.has(RecievingThirstDamageKey)) {
            PDC.remove(RecievingThirstDamageKey);
            DealThirstDamage(player);
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer PDC = player.getPersistentDataContainer();
        PDC.set(ThirstKey, PersistentDataType.INTEGER, 20);
        if (PDC.has(DiedOfThirstKey)) {
            PDC.remove(DiedOfThirstKey);
            event.deathMessage(Component.text(player.getName() + " died of thirst"));
        }
    }
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack ItemEaten = event.getItem();
        Material material = ItemEaten.getType();
        if (material == Material.MILK_BUCKET || material == Material.HONEY_BOTTLE || material == Material.POTION) {
            Player player = event.getPlayer();
            PersistentDataContainer PDC = player.getPersistentDataContainer();
            int thirst = PDC.get(ThirstKey, PersistentDataType.INTEGER);
            if (material == Material.MILK_BUCKET) {
                thirst += 2;
            }
            else if (material == Material.HONEY_BOTTLE) {
                thirst += 1;
            }
            else if (material == Material.POTION) {
                PotionMeta meta = (PotionMeta) ItemEaten.getItemMeta();
                if (meta.getBasePotionType() == PotionType.WATER) {
                    thirst += 3;
                    if (meta.getPersistentDataContainer().has(FlaskKey)) {
                        int water_level = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(meta.lore().get(0)).replace("/100", "")) - 1;
                        if (water_level == 0) {
                            event.setItem(EmptyWaterFlask);
                        }
                        else {
                            event.setItem(WaterFlask(water_level));
                        }
                    }
                }
                else {
                    thirst += 1;
                }
            }
            thirst = Math.min(20, thirst);
            PDC.set(ThirstKey, PersistentDataType.INTEGER, thirst);
            if (thirst > 9) {
                PotionEffect slowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
                if (slowness != null && slowness.isInfinite()) {
                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                }
            }
            else if (thirst > 6) {
                PotionEffect nausea = player.getPotionEffect(PotionEffectType.NAUSEA);
                if (nausea != null && nausea.isInfinite()) {
                    player.removePotionEffect(PotionEffectType.NAUSEA);
                }
            }
            else if (thirst > 3) {
                PotionEffect MiningFatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                if (MiningFatigue != null && MiningFatigue.isInfinite()) {
                    player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                }
            }
        }
    }
    @EventHandler
    public void onBottleFill(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            ItemStack item = event.getItem();
            if (item != null) {
                ItemMeta ItemMeta = item.getItemMeta();
                if (item.getType() == Material.GLASS_BOTTLE || (item.getType() == Material.POTION && ((PotionMeta) ItemMeta).getBasePotionType() == PotionType.WATER)) {
                    if (ItemMeta.getPersistentDataContainer().has(FlaskKey)) {
                        Player player = event.getPlayer();
                        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 4.5, FluidCollisionMode.SOURCE_ONLY);
                        if (result != null && result.getHitBlock() != null && result.getHitBlock().getType() == Material.WATER) {
                            event.setCancelled(true);
                            PlayerInventory inventory = player.getInventory();
                            if (event.getHand() == EquipmentSlot.HAND) {
                                inventory.setItemInMainHand(WaterFlask(100));
                            }
                            else {
                                inventory.setItemInOffHand(WaterFlask(100));
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getFoodLevel() > event.getFoodLevel()) {
                PersistentDataContainer PDC = player.getPersistentDataContainer();
                int thirst = PDC.get(ThirstKey, PersistentDataType.INTEGER);
                thirst = Math.max(0, thirst - 1);
                PDC.set(ThirstKey, PersistentDataType.INTEGER, thirst);
                if (thirst <= 3) {
                    PotionEffect slowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
                    if (slowness == null || !slowness.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 3));
                    }
                    PotionEffect nausea = player.getPotionEffect(PotionEffectType.NAUSEA);
                    if (nausea == null || !nausea.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, PotionEffect.INFINITE_DURATION, 0));
                    }
                    PotionEffect MiningFatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE);
                    if (MiningFatigue == null || !MiningFatigue.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 0));
                    }
                }
                else if (thirst <= 6) {
                    PotionEffect slowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
                    if (slowness == null || !slowness.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 3));
                    }
                    PotionEffect nausea = player.getPotionEffect(PotionEffectType.NAUSEA);
                    if (nausea == null || !nausea.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, PotionEffect.INFINITE_DURATION, 0));
                    }
                }
                else if (thirst <= 9) {
                    PotionEffect slowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
                    if (slowness == null || !slowness.isInfinite()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 3));
                    }
                }
                if (thirst == 0) {
                    DealThirstDamage(player);
                }
            }
        }
    }
    @EventHandler
    public void onMilk(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == EntityPotionEffectEvent.Cause.MILK) {
                if (event.getOldEffect().isInfinite()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    @Override
    public void onDisable() {
        getLogger().info("Thirst System has been disabled");
    }
}