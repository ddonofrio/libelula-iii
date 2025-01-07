package me.libelula.spawn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class EventManager implements Listener {

    private final Spawn plugin;

    public EventManager(Spawn plugin) {
        this.plugin = plugin;
    }

    // Player Events
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getSpawnPointManager().handlePlayerJoin(player, plugin.getPermissionManager());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPermissionManager().isAdmin(player) &&
                plugin.getRegionManager().isLocationInSpawn(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        if (plugin.getSpawnSetup().isUserInSetup(player)) {
            if (message.equals("/spawn end")) {
                event.setCancelled(true);
                plugin.getSpawnSetup().endSetup(player);
            } else if (message.equals("/spawn next")) {
                event.setCancelled(true);
                plugin.getCommandManager().processCommand(player, message);
            } else if (!message.startsWith("/spawn")) {
                event.setCancelled(true);
                player.sendMessage("§cYou are in setup mode. Use §e/spawn end §cto abort setup.");
            }
            return;
        }

        if (message.startsWith("/spawn")) {
            event.setCancelled(true);
            plugin.getCommandManager().processCommand(player, message);
        } else if (plugin.getRegionManager().isLocationInSpawn(player.getLocation()) &&
                !plugin.getPermissionManager().isAdmin(player)) {
            event.setCancelled(true);
            player.sendMessage("§cCommands are disabled in the spawn region.");
        }
    }


    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location portalLocation = event.getFrom();
        if (plugin.getRegionManager().isLocationInSpawn(portalLocation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPermissionManager().isAdmin(player) &&
                plugin.getRegionManager().isLocationInSpawn(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (!plugin.getPermissionManager().isAdmin(player) &&
                    plugin.getRegionManager().isLocationInSpawn(player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPermissionManager().isAdmin(player) &&
                plugin.getRegionManager().isLocationInSpawn(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    // Entity Events
    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        Location portalLocation = event.getLocation();
        if (plugin.getRegionManager().isLocationInSpawn(portalLocation)) {
            event.getEntity().remove();
        }
    }

    @EventHandler
    public void onEntityPortalExit(EntityPortalExitEvent event) {
        Location portalLocation = event.getEntity().getLocation();
        if (plugin.getRegionManager().isLocationInSpawn(portalLocation)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getEntity().getLocation())) {
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getEntity().getLocation())) {
            if (event.getEntity() instanceof Animals) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    // Block Events
    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireSpread(BlockBurnEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSpawnSetup().isUserInSetup(player)) {
            event.setCancelled(true);
            plugin.getSpawnSetup().addSpawnPoint(player, event.getBlock().getLocation());
            return;
        }

        if (!plugin.getPermissionManager().isAdmin(player) &&
                plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setDropItems(false);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPermissionManager().isAdmin(player) &&
                plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            if (event.getBlock().getType() == Material.ICE || event.getBlock().getType() == Material.FROSTED_ICE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (plugin.getRegionManager().isLocationInSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
