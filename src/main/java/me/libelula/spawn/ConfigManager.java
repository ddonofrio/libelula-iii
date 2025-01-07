package me.libelula.spawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final Spawn plugin;
    private File configFile;
    private FileConfiguration config;
    private List<Location> spawnPoints;
    private World spawnWorld;
    private ProtectedRegion spawnRegion;
    private int teleportDelay;

    public ConfigManager(Spawn plugin) {
        this.plugin = plugin;
        this.spawnPoints = new ArrayList<>();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!configFile.exists()) {
            copyDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration loaded.");
        verifyRegionConfig();
        verifyTeleportMode();
        teleportDelay = config.getInt("teleport-delay", 3);
        loadSpawnPoints();
    }

    private void copyDefaultConfig() {
        try (InputStream in = plugin.getResource("spawn.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Default configuration file not found in resources.");
                return;
            }
            Files.copy(in, configFile.toPath());
            plugin.getLogger().info("Default configuration 'spawn.yml' created.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not copy default configuration: " + e.getMessage());
        }
    }

    private void loadSpawnPoints() {
        spawnPoints.clear();
        List<?> rawPoints = config.getList("spawnpoints");
        if (rawPoints != null) {
            for (Object point : rawPoints) {
                if (point instanceof String) {
                    String[] parts = ((String) point).split(",");
                    if (parts.length == 6) {
                        try {
                            World world = Bukkit.getWorld(parts[0]);
                            double x = Double.parseDouble(parts[1]);
                            double y = Double.parseDouble(parts[2]);
                            double z = Double.parseDouble(parts[3]);
                            float yaw = Float.parseFloat(parts[4]);
                            float pitch = Float.parseFloat(parts[5]);
                            if (world != null) {
                                spawnPoints.add(new Location(world, x, y, z, yaw, pitch));
                            }
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid spawn point format: " + point);
                        }
                    }
                }
            }
        }
    }

    public List<Location> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    public void addSpawnPoint(Location location) {
        spawnPoints.add(location);
        saveSpawnPoints();
    }

    public void clearSpawnPoints() {
        spawnPoints.clear();
        saveSpawnPoints();
    }

    private void saveSpawnPoints() {
        List<String> rawPoints = new ArrayList<>();
        for (Location location : spawnPoints) {
            String formattedLocation = String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
                    location.getWorld().getName(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch());
            rawPoints.add(formattedLocation);
        }
        config.set("spawnpoints", rawPoints);
        saveConfig();
    }

    public boolean verifyRegionConfig() {
        String worldName = config.getString("spawn-region.world");
        String regionName = config.getString("spawn-region.region");

        if (worldName == null || regionName == null) {
            plugin.getLogger().severe("Mandatory configuration keys 'world' or 'region' are missing.");
            spawnWorld = null;
            spawnRegion = null;
            return false;
        }

        spawnWorld = Bukkit.getWorld(worldName);
        if (spawnWorld == null) {
            plugin.getLogger().severe("Configured world '" + worldName + "' does not exist.");
            spawnRegion = null;
            return false;
        }

        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(spawnWorld));
        if (regionManager == null) {
            plugin.getLogger().severe("No RegionManager found for world '" + worldName + "'.");
            spawnRegion = null;
            return false;
        }

        spawnRegion = regionManager.getRegion(regionName);
        if (spawnRegion == null) {
            plugin.getLogger().warning("The configured spawn region '" + regionName + "' does not exist.");
            plugin.getLogger().warning("Please create it in WorldGuard or update spawn.yml.");
            return false;
        }

        return true;
    }

    public World getSpawnWorld() {
        return spawnWorld;
    }

    public ProtectedRegion getSpawnRegion() {
        return spawnRegion;
    }

    public void verifyTeleportMode() {
        String mode = config.getString("teleport-mode", "shuffle").toLowerCase();
        if (!mode.equals("sequential") && !mode.equals("shuffle") && !mode.equals("random")) {
            plugin.getLogger().severe("Invalid teleport mode: " + mode + ". Defaulting to 'shuffle'.");
            config.set("teleport-mode", "shuffle");
            saveConfig();
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
            plugin.getLogger().info("Configuration saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save configuration: " + e.getMessage());
        }
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public FileConfiguration getConfig() {
        return config;
    }

}
