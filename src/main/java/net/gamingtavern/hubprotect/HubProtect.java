package net.gamingtavern.hubprotect;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class HubProtect extends JavaPlugin implements Listener {
    private final List<Material> cancelledMaterials = new ArrayList<>();
    private final List<EntityDamageEvent.DamageCause> cancelledDamageCauses = new ArrayList<>();
    private Logger logger;
    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = Bukkit.getLogger();

        logger.info("Registering Events");
        Bukkit.getPluginManager().registerEvents(this, this);

        logger.info("Loading config.yml");
        loadConfig();
    }

    private void loadConfig() {
        saveDefaultConfig(); // This will copy the config.yml from the JAR if it doesn't exist in the plugin folder.

        // Load the configuration file
        FileConfiguration config = getConfig();

        // Read the list of materials from the config.yml
        List<String> materialsList = config.getStringList("cancel-interact");

        // Clear the existing list before adding the new materials
        cancelledMaterials.clear();

        // Parse the list of materials and add them to the cancelledMaterials list
        for (String materialString : materialsList) {
            try {
                Material material = Material.valueOf(materialString);
                cancelledMaterials.add(material);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material found in config.yml: " + materialString);
            }
        }

        logger.info("Loaded " + cancelledMaterials.size() + " materials from config.yml");

        // Read the list of damage causes from the config.yml
        List<String> damageCausesList = config.getStringList("cancel-damage");

        // Clear the existing list before adding the new damage causes
        cancelledDamageCauses.clear();

        for (String damageCauseString : damageCausesList) {
            try {
                EntityDamageEvent.DamageCause damageCause = EntityDamageEvent.DamageCause.valueOf(damageCauseString);
                cancelledDamageCauses.add(damageCause);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid damage cause found in config.yml: " + damageCauseString);
            }
        }

        logger.info("Loaded " + cancelledDamageCauses.size() + " damage causes from config.yml");
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();

        if (block == null) return;

        Material clickedMaterial = block.getType();

        GameMode gameMode = event.getPlayer().getGameMode();

        if (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL) {
            if (cancelledMaterials.contains(clickedMaterial)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void EntityDamageEvent (EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;

        if (cancelledDamageCauses.contains(event.getCause())) {
            event.setCancelled(true);
        }
    }
}
