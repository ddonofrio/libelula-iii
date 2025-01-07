package me.libelula.spawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SpawnSetup {

    private final Spawn plugin;
    private final Map<Player, Integer> setupUsers = new HashMap<>();

    public SpawnSetup(Spawn plugin) {
        this.plugin = plugin;
    }

    public boolean isUserInSetup(Player player) {
        return setupUsers.containsKey(player);
    }

    public void startSetup(Player player) {
        setupUsers.put(player, 0);
        player.sendMessage("§aBreak blocks to add spawnpoints or type §e/spawn next §ato continue.");
    }

    public void endSetup(Player player) {
        setupUsers.remove(player);
    }

    public void addSpawnPoint(Player player, Location location) {
        int spawnIndex = setupUsers.get(player);
        plugin.getConfigManager().addSpawnPoint(location);
        player.sendMessage("§aSpawnpoint " + spawnIndex + " added at " +
                "X: " + location.getBlockX() + ", Y: " + location.getBlockY() + ", Z: " + location.getBlockZ());
        setupUsers.put(player, spawnIndex + 1);
    }

    public void saveConfiguration(Player player) {
        player.sendMessage("§aSpawnpoints have been saved successfully.");
        endSetup(player);
    }

}
