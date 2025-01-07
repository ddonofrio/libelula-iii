package me.libelula.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class SpawnPointManager {

    private final Spawn plugin;
    private Queue<Location> sequentialQueue;
    private final Random random;
    private String teleportMode;

    public SpawnPointManager(Spawn plugin) {
        this.plugin = plugin;
        this.random = new Random();
        loadConfiguration();
    }

    public void loadConfiguration() {
        ConfigManager configManager = plugin.getConfigManager();
        this.teleportMode = configManager.getConfig().getString("teleport-mode", "shuffle").toLowerCase();

        if (teleportMode.equals("sequential") || teleportMode.equals("shuffle")) {
            this.sequentialQueue = new LinkedList<>(configManager.getSpawnPoints());
        }
    }

    public void handlePlayerJoin(Player player, PermissionManager permissionManager) {
        if (!plugin.getConfigManager().getSpawnPoints().isEmpty()) {
            if (permissionManager.isAdmin(player)) {
                player.sendMessage("§cNo spawn configured. Please type §e/spawn setup§c to configure.");
            }
            return;
        }

        if (!permissionManager.hasBypassPermission(player)) {
            teleportPlayer(player);
        }
    }

    public void teleportPlayer(Player player) {
        List<Location> spawnPoints = plugin.getConfigManager().getSpawnPoints();

        if (spawnPoints.isEmpty()) {
            plugin.getLogger().warning("No spawnpoints configured. Cannot teleport player.");
            return;
        }

        Location spawnPoint = null;

        switch (teleportMode) {
            case "sequential":
                if (sequentialQueue.isEmpty()) {
                    sequentialQueue.addAll(spawnPoints);
                }
                spawnPoint = sequentialQueue.poll();
                break;

            case "shuffle":
                if (sequentialQueue.isEmpty()) {
                    sequentialQueue.addAll(spawnPoints);
                }
                spawnPoint = pickRandomFromQueue();
                break;

            case "random":
            default:
                spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size()));
                break;
        }

        if (spawnPoint != null) {
            Location finalSpawnPoint = spawnPoint;
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(finalSpawnPoint), 2L);
        }
    }

    private Location pickRandomFromQueue() {
        int randomIndex = random.nextInt(sequentialQueue.size());
        Location[] queueArray = sequentialQueue.toArray(new Location[0]);
        Location selected = queueArray[randomIndex];
        sequentialQueue.remove(selected);
        return selected;
    }

    public boolean isConfigurationValid() {
        return !plugin.getConfigManager().getSpawnPoints().isEmpty();
    }
}
