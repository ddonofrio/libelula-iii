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
import java.util.*;

public class ConfigManager {

    private final Spawn plugin;
    private File configFile;
    private FileConfiguration config;

    private List<SpawnRegion> spawnRegions = new ArrayList<>();
    private String teleportMode = "shuffle";
    private int teleportDelay = 3;
    private int teleportYOffset = 1;

    public ConfigManager(Spawn plugin) {
        this.plugin = plugin;
        loadConfig();
        validateConfig();
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("Creating default configuration file...");
            createDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Configuration loaded from spawn.yml.");

        loadSpawnRegions();
        teleportMode = config.getString("teleport-mode", "shuffle");
        teleportDelay = config.getInt("teleport-delay", 3);
        teleportYOffset = config.getInt("teleport-y-offset", 1);
    }

    private void createDefaultConfig() {
        try (InputStream in = plugin.getResource("spawn.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Default config not found in resources!");
                return;
            }
            Files.copy(in, new File(plugin.getDataFolder(), "spawn.yml").toPath());
            plugin.getLogger().info("Default config file created.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default config: " + e.getMessage());
        }
    }

    private void loadSpawnRegions() {
        spawnRegions.clear();
        List<Map<?, ?>> regionsConfig = config.getMapList("spawn-regions");
        for (Map<?, ?> regionData : regionsConfig) {
            if (!(regionData.containsKey("world") && regionData.containsKey("region"))) {
                plugin.getLogger().warning("Incomplete region data, skipping...");
                continue;
            }
            String worldName = regionData.get("world").toString();
            String regionName = regionData.get("region").toString();
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World not found: " + worldName + ", skipping...");
                continue;
            }
            RegionManager regionManager = WorldGuard.getInstance()
                    .getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(world));
            if (regionManager == null) {
                plugin.getLogger().warning("No RegionManager for " + worldName + ", skipping...");
                continue;
            }
            ProtectedRegion protectedRegion = regionManager.getRegion(regionName);
            if (protectedRegion == null) {
                plugin.getLogger().warning("Region not found: " + regionName + ", skipping...");
                continue;
            }
            List<Location> spawnPoints = new ArrayList<>();
            Object rawPointsObj = regionData.get("spawnpoints");
            if (rawPointsObj instanceof List<?> rawPoints) {
                for (Object point : rawPoints) {
                    if (point instanceof String) {
                        String[] parts = ((String) point).split(",");
                        if (parts.length >= 5) {
                            try {
                                double x = Double.parseDouble(parts[0]);
                                double y = Double.parseDouble(parts[1]);
                                double z = Double.parseDouble(parts[2]);
                                float yaw = Float.parseFloat(parts[3]);
                                float pitch = Float.parseFloat(parts[4]);
                                spawnPoints.add(new Location(world, x, y, z, yaw, pitch));
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Invalid spawn point: " + point);
                            }
                        }
                    }
                }
            }
            spawnRegions.add(new SpawnRegion(world, protectedRegion, spawnPoints));
            plugin.getLogger().info("Loaded region " + regionName + " in " + worldName);
        }
    }

    private void validateConfig() {
        if (spawnRegions.isEmpty()) {
            plugin.getLogger().warning("No valid spawn regions found.");
        }
        if (!Arrays.asList("sequential", "shuffle", "random").contains(teleportMode)) {
            plugin.getLogger().warning("Invalid teleport mode: " + teleportMode
                    + ", defaulting to 'shuffle'.");
            teleportMode = "shuffle";
        }
        if (teleportDelay < 0) {
            plugin.getLogger().warning("Teleport delay < 0, defaulting to 3.");
            teleportDelay = 3;
        }
    }

    // ========== Getters ==========
    public List<SpawnRegion> getSpawnRegions() {
        return new ArrayList<>(spawnRegions);
    }

    public String getTeleportMode() {
        return teleportMode;
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public int getTeleportYOffset() {
        return teleportYOffset;
    }

    // ========== Setters ==========
    public void setTeleportMode(String mode) {
        if (Arrays.asList("sequential", "shuffle", "random").contains(mode)) {
            teleportMode = mode;
        } else {
            plugin.getLogger().warning("Invalid mode: " + mode + ", ignoring.");
        }
    }

    public void setTeleportDelay(int delay) {
        if (delay >= 0) {
            teleportDelay = delay;
        } else {
            plugin.getLogger().warning("Delay < 0, ignoring.");
        }
    }

    public void setTeleportYOffset(int offset) {
        teleportYOffset = offset;
    }

    /**
     * Add a new spawn point to an existing region.
     * If region not found, logs warning and does nothing.
     * Saves config after insertion.
     */
    public void addSpawnPoint(World world, String regionName, Location location) {
        if (world == null || regionName == null || location == null) {
            plugin.getLogger().warning("Invalid data for addSpawnPoint");
            return;
        }
        for (SpawnRegion sr : spawnRegions) {
            if (sr.getWorld().equals(world)
                    && sr.getRegion().getId().equals(regionName)) {
                sr.addSpawnPoint(location);
                plugin.getLogger().info("Spawn point added to region "
                        + regionName + " in " + world.getName());
                saveConfig();
                return;
            }
        }
        plugin.getLogger().warning("Region [" + regionName
                + "] not found in world [" + world.getName() + "].");
    }

    public void saveConfig() {
        List<Map<String, Object>> regionsConfig = new ArrayList<>();
        for (SpawnRegion sr : spawnRegions) {
            Map<String, Object> regionData = new HashMap<>();
            regionData.put("world", sr.getWorld().getName());
            regionData.put("region", sr.getRegion().getId());

            List<String> rawPoints = new ArrayList<>();
            for (Location loc : sr.getSpawnPoints()) {
                String fmt = String.format("%.2f,%.2f,%.2f,%.2f,%.2f",
                        loc.getX(), loc.getY(), loc.getZ(),
                        loc.getYaw(), loc.getPitch());
                rawPoints.add(fmt);
            }
            regionData.put("spawnpoints", rawPoints);
            regionsConfig.add(regionData);
        }
        config.set("spawn-regions", regionsConfig);
        config.set("teleport-mode", teleportMode);
        config.set("teleport-delay", teleportDelay);
        config.set("teleport-y-offset", teleportYOffset);

        try {
            config.save(configFile);
            plugin.getLogger().info("Configuration saved to spawn.yml.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config: " + e.getMessage());
        }
    }

    public void clearSpawnPointsInRegion(String regionName) {
        for (SpawnRegion sr : spawnRegions) {
            if (sr.getRegion().getId().equals(regionName)) {
                sr.clearSpawnPoints();
                plugin.getLogger().info("Spawn points cleared for region: " + regionName);
                saveConfig();
                return;
            }
        }
        plugin.getLogger().warning("Region " + regionName + " not found. No points cleared.");
    }
}
