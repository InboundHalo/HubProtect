package net.gamingtavern.hubprotect;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public final class HubProtect extends JavaPlugin implements Listener {
    private final List<Material> cancelledMaterials = new ArrayList<>();
    private boolean cancel;
    private final List<EntityDamageEvent.DamageCause> cancelledDamageCauses = new ArrayList<>();
    private Logger logger;

    @Override
    public void onEnable() {
        logger = Bukkit.getLogger();
        logger.info("Registering Events");
        Bukkit.getPluginManager().registerEvents(this, this);
        logger.info("Loading config.yml");
        loadConfig();

        for (World world : Bukkit.getServer().getWorlds()) {
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.FALL_DAMAGE,  false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
        }
    }

    private void loadConfig() {
        saveDefaultConfig();

        FileConfiguration config = getConfig();

        cancel = config.getBoolean("cancel-interact", false);

        // Load and parse the list of materials
        cancelledMaterials.clear();
        for (String materialString : config.getStringList("cancel-interact")) {
            try {
                Material material = Material.valueOf(materialString);
                cancelledMaterials.add(material);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material found in config.yml: " + materialString);
            }
        }
        logger.info("Loaded " + cancelledMaterials.size() + " materials from config.yml");

        // Load and parse the list of damage causes
        cancelledDamageCauses.clear();
        for (String damageCauseString : config.getStringList("cancel-damage")) {
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

        GameMode gameMode = event.getPlayer().getGameMode();
        boolean creative = event.getPlayer().getGameMode() == GameMode.CREATIVE;

        if (creative) {
            return;
        }

        if (cancel) {
            event.setCancelled(true);
            return;
        }

        if (block == null) return;

        Material clickedMaterial = block.getType();

        if (cancelledMaterials.contains(clickedMaterial)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (cancelledDamageCauses.contains(event.getCause())) {
                event.setCancelled(true);
            }
        }
    }
}
