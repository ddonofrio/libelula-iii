package me.libelula.spawn;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandManager {

    private final Spawn plugin;

    public CommandManager(Spawn plugin) {
        this.plugin = plugin;
    }

    /**
     * Processes the command text from a player.
     * Expected format: /spawn [subcommand]
     */
    public void processCommand(Player player, String commandText) {
        String[] args = commandText.trim().split("\\s+");

        // Just "/spawn" or "/spawn help"
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("/spawn")) {
                handleSpawnCommand(player);
                return;
            } else if (args[0].equalsIgnoreCase("/spawn help")) {
                sendHelp(player);
                return;
            }
        }

        // Any other subcommand requires player to be in a spawn region
        ProtectedRegion region = plugin.getRegionManager()
                .getRegionForLocation(player.getLocation());
        if (region == null) {
            player.sendMessage("§cYou must be in a spawn region to use this command.");
            return;
        }
        String regionName = region.getId();

        // Admin-only commands
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "addsp":
                    handleAddSpCommand(player, regionName);
                    break;
                case "clear":
                    handleClearCommand(player, regionName);
                    break;
                case "allow":
                    handleAllowCommand(player, regionName);
                    break;
                case "allow-command":
                    handleAllowCommandCommand(player, regionName);
                    break;
                default:
                    player.sendMessage("§cInvalid usage. Try /spawn help.");
                    break;
            }
        } else {
            player.sendMessage("§cInvalid usage of /spawn. Use /spawn help for assistance.");
        }
    }

    /**
     * Teleport the player to the next spawnpoint (first region for now).
     */
    private void handleSpawnCommand(Player player) {
        int highestSpawn = plugin.getPermissionManager().getHighestSpawnPermission(player);
        if (highestSpawn == -1) {
            player.sendMessage("§cYou do not have permission to teleport to any spawn.");
            return;
        }
        List<SpawnRegion> regions = plugin.getConfigManager().getSpawnRegions();
        if (highestSpawn >= regions.size()) {
            highestSpawn = regions.size() - 1; // Adjust to the last valid spawn
        }
        SpawnRegion targetRegion = regions.get(highestSpawn);
        Location spawnLocation = plugin.getSpawnPointManager()
                .getNextSpawnPoint(targetRegion.getRegion().getId(), player.getLocation());
        Location initialLocation = player.getLocation();

        int teleportDelay = plugin.getConfigManager().getTeleportDelay();
        if (!plugin.getPermissionManager().hasDelayBypassPermission(player) && teleportDelay > 0) {
            player.sendMessage("§eTeleporting in " + teleportDelay + " seconds...");
            int finalHighestSpawn = highestSpawn;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.getLocation().equals(initialLocation)) {
                    player.sendMessage("§cTeleport cancelled because you moved.");
                    return;
                }
                player.teleport(spawnLocation);
                player.sendMessage("§aTeleported to spawn " + finalHighestSpawn + ".");
            }, teleportDelay * 20L); // Convert seconds to ticks
        } else {
            player.teleport(spawnLocation);
            player.sendMessage("§aTeleported to spawn " + highestSpawn + ".");
        }
    }

    /**
     * Show help messages.
     */
    private void sendHelp(Player player) {
        if (plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§aAdmin Commands:");
            player.sendMessage("§e/spawn addsp §7- Add a new spawn point in this region.");
            player.sendMessage("§e/spawn clear §7- Clear all spawn points in this region.");
            player.sendMessage("§e/spawn allow §7- (Demo) Allow certain block interactions.");
            player.sendMessage("§e/spawn allow-command §7- (Demo) Allow certain commands here.");
        } else {
            player.sendMessage("§aUser Commands:");
            player.sendMessage("§e/spawn §7- Teleport to the next spawn point of the first region.");
        }
    }

    /**
     * /spawn addsp: Adds a spawn point to the current region.
     */
    private void handleAddSpCommand(Player player, String regionName) {
        if (!plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§cOnly administrators can use this command.");
            return;
        }
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            player.sendMessage("§cYou must be in creative mode to use this command.");
            return;
        }
        Location loc = player.getLocation();
        plugin.getSpawnPointManager().setSpawnPoint(regionName, loc);
        player.sendMessage("§aSpawn point added to region: " + regionName);
    }

    /**
     * /spawn clear: Clears all spawn points in the current region.
     */
    private void handleClearCommand(Player player, String regionName) {
        if (!plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        plugin.getConfigManager().clearSpawnPointsInRegion(regionName);
        plugin.getSpawnPointManager().reloadData();
        player.sendMessage("§aAll spawn points cleared for region: " + regionName);
    }



    /**
     * /spawn allow: Example placeholder command for allowing interactions.
     */
    private void handleAllowCommand(Player player, String regionName) {
        if (!plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        // This is just a placeholder—real logic might store data in config.
        player.sendMessage("§aAllowed interactions in region: " + regionName);
    }

    /**
     * /spawn allow-command: Example placeholder command for allowing commands.
     */
    private void handleAllowCommandCommand(Player player, String regionName) {
        if (!plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }
        // Another placeholder
        player.sendMessage("§aAllowed commands in region: " + regionName);
    }
}
