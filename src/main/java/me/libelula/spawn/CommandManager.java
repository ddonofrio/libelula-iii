package me.libelula.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandManager {

    private final Spawn plugin;

    public CommandManager(Spawn plugin) {
        this.plugin = plugin;
    }

    public void processCommand(Player player, String commandText) {
        String[] args = commandText.split(" ");
        int teleportDelay = plugin.getConfigManager().getTeleportDelay();

        if (args.length == 1) {
            if (!plugin.getSpawnPointManager().isConfigurationValid()) {
                player.sendMessage("§cNo spawn points configured. Please contact the server owner.");
                return;
            }

            if (plugin.getPermissionManager().isAdmin(player)) {
                plugin.getSpawnPointManager().teleportPlayer(player);
                player.sendMessage("§aYou have been teleported to the spawn.");
            } else {
                player.sendMessage("§aDon't move for " + teleportDelay + " seconds to be teleported to the spawn.");

                Location initialLocation = player.getLocation();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!player.getLocation().equals(initialLocation)) {
                        player.sendMessage("§cTeleportation cancelled. You moved!");
                    } else {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (player.getLocation().equals(initialLocation)) {
                                plugin.getSpawnPointManager().teleportPlayer(player);
                                player.sendMessage("§aYou have been teleported to the spawn.");
                            } else {
                                player.sendMessage("§cTeleportation cancelled. You moved!");
                            }
                        }, teleportDelay * 10L);
                    }
                }, teleportDelay * 10L);
            }
        } else if (args.length == 2 && args[1].equalsIgnoreCase("setup")) {
            handleSetupCommand(player);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
            sendHelp(player);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("next")) {
            handleNextCommand(player);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("end")) {
            handleEndCommand(player);
        } else if (args.length == 2 && args[1].equalsIgnoreCase("clear")) {
            handleClearCommand(player);
        } else {
            player.sendMessage("§cInvalid usage of /spawn. Use /spawn help for assistance.");
        }
    }

    private void handleSetupCommand(Player player) {
        if (!plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§cOnly administrators can use this command.");
            return;
        }

        if (plugin.getSpawnPointManager().isConfigurationValid()) {
            player.sendMessage("§cSpawnpoints are already configured.");
            player.sendMessage("§eUse /spawn addspawnpoint to add more or /spawn clear to reset.");
        } else if (!player.getGameMode().equals(org.bukkit.GameMode.CREATIVE)) {
            player.sendMessage("§cYou must be in creative mode to use this command.");
        } else {
            plugin.getSpawnSetup().startSetup(player);
        }
    }

    private void handleClearCommand(Player player) {
        if (plugin.getPermissionManager().isAdmin(player)) {
            plugin.getConfigManager().clearSpawnPoints();
            player.sendMessage("§aAll spawn points have been cleared.");
        } else {
            player.sendMessage("§cYou do not have permission to use this command.");
        }
    }

    private void handleNextCommand(Player player) {
        if (!plugin.getSpawnSetup().isUserInSetup(player)) {
            player.sendMessage("§cYou are not in setup mode.");
            return;
        }

        if (plugin.getConfigManager().getSpawnPoints().isEmpty()) {
            player.sendMessage("§cYou must configure at least one spawnpoint before proceeding.");
            return;
        }

        plugin.getSpawnSetup().saveConfiguration(player);
        player.sendMessage("§aSpawn setup completed successfully.");
    }

    private void handleEndCommand(Player player) {
        if (plugin.getSpawnSetup().isUserInSetup(player)) {
            plugin.getSpawnSetup().endSetup(player);
            player.sendMessage("§cSetup aborted. Your changes have not been saved.");
        } else {
            player.sendMessage("§cYou are not in setup mode.");
        }
    }

    private void sendHelp(Player player) {
        if (plugin.getPermissionManager().isAdmin(player)) {
            player.sendMessage("§aAdmin Commands:");
            player.sendMessage("§e/spawn setup §7- Configure spawn points.");
            player.sendMessage("§e/spawn clear §7- Clear all spawn points.");
            player.sendMessage("§e/spawn allow §7- Allow users to interact with specific blocks.");
            player.sendMessage("§e/spawn allow-command §7- Allow users to use commands in the spawn region.");
        } else {
            player.sendMessage("§aUser Commands:");
            player.sendMessage("§e/spawn §7- Teleport to the spawn point.");
        }
    }
}
