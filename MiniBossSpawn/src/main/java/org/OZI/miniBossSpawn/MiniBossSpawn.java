package org.OZI.miniBossSpawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import static org.OZI.miniBossSpawn.Utils.ChaatColor.color;

public class MiniBossSpawn extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("MinibossPlugin włączony!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cTylko gracz może użyć tej komendy!"));
            return true;
        }

        Player player = (Player) sender;

        if (!command.getName().equalsIgnoreCase("spawnboss")) return true;

        if (args.length != 1) {
            player.sendMessage(color("&cUżycie: /spawnboss <nazwa>"));
            return true;
        }

        if (config.getConfigurationSection("bosses") == null) {
            player.sendMessage(color("&cBrak bossów w configu!"));
            return true;
        }

        String bossKey = args[0].toLowerCase();

        if (!config.getConfigurationSection("bosses").contains(bossKey)) {
            player.sendMessage(color("&cNie znaleziono bossa o takiej nazwie!"));
            return true;
        }

        Location loc = player.getLocation().add(2, 0, 0);
        Zombie boss = player.getWorld().spawn(loc, Zombie.class);

        String path = "bosses." + bossKey;

        String displayName = color(config.getString(path + ".displayName"));
        double health = config.getDouble(path + ".health");
        int speed = config.getInt(path + ".speed");

        boss.setCustomName(displayName);
        boss.setCustomNameVisible(true);

        // ustawienie HP
        boss.setMaxHealth(health);
        boss.setHealth(health);

        // Speed efekt
        if (speed > 0) {
            boss.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED,
                    Integer.MAX_VALUE,
                    speed - 1
            ));
        }

        player.sendMessage(color("&aBoss " + displayName + " pojawił się przed Tobą!"));
        return true;
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Zombie)) return;

        Zombie boss = (Zombie) event.getEntity();

        if (boss.getCustomName() == null) return;
        if (config.getConfigurationSection("bosses") == null) return;

        String bossName = boss.getCustomName();

        for (String key : config.getConfigurationSection("bosses").getKeys(false)) {

            String displayName = color(config.getString("bosses." + key + ".displayName"));
            if (!displayName.equals(bossName)) continue;

            event.getDrops().clear();

            Player killer = boss.getKiller();
            if (killer == null) return;

            List<String> drops = config.getStringList("bosses." + key + ".drops");

            for (String drop : drops) {
                String[] parts = drop.split(":");
                if (parts.length != 2) continue;

                Material mat = Material.getMaterial(parts[0].toUpperCase());
                if (mat == null) continue;

                int amount;
                try {
                    amount = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                killer.getInventory().addItem(new ItemStack(mat, amount));
            }

            killer.sendMessage(color("&6Zdobyłeś nagrody za pokonanie " + bossName + "!"));
            return;
        }
    }
}
