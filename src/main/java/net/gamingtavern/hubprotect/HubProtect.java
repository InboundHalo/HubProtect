package net.gamingtavern.hubprotect;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class HubProtect extends JavaPlugin implements Listener {
    private final List<Material> cancelledMaterials = new ArrayList<>();
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
}
