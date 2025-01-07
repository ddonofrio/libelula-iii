package me.libelula.spawn;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class SpawnPointManager {

    private final Spawn plugin;
    private final Map<String, List<Location>> regionSpawnPoints = new HashMap<>();
    // Used to track last index for sequential/shuffle modes
    private final Map<String, Integer> regionIndexes = new HashMap<>();
    // Shuffle data
    private final Map<String, List<Location>> regionShuffleOrder = new HashMap<>();

    private String teleportMode;
    private int teleportYOffset;

    public SpawnPointManager(Spawn plugin) {
        this.plugin = plugin;
        reloadData();
    }

    /**
     * Reloads data from config (teleport mode, offset, spawnPoints).
     */
    public void reloadData() {
        this.teleportMode = plugin.getConfigManager().getTeleportMode();
        this.teleportYOffset = plugin.getConfigManager().getTeleportYOffset();
        regionSpawnPoints.clear();
        regionIndexes.clear();
        regionShuffleOrder.clear();

        // Cache spawn regions in memory
        for (SpawnRegion sr : plugin.getConfigManager().getSpawnRegions()) {
            String regionId = sr.getRegion().getId();
            List<Location> spawns = sr.getSpawnPoints();
            regionSpawnPoints.put(regionId, spawns);
            regionIndexes.put(regionId, 0);
            regionShuffleOrder.put(regionId, new ArrayList<>(spawns));
            // Pre-shuffle once for "shuffle" mode
            Collections.shuffle(regionShuffleOrder.get(regionId));
        }
        plugin.getLogger().info("SpawnPointManager data reloaded.");
    }

    /**
     * Returns the next spawn point based on teleport mode and offset.
     * If region not found or no spawns, returns fallback.
     */
    public Location getNextSpawnPoint(String regionName, Location fallback) {
        List<Location> spawns = regionSpawnPoints.get(regionName);
        if (spawns == null || spawns.isEmpty()) {
            return fallback;
        }
        switch (teleportMode) {
            case "sequential":
                return getSequentialSpawn(regionName, spawns, fallback);
            case "random":
                return getRandomSpawn(spawns, fallback);
            case "shuffle":
            default:
                return getShuffleSpawn(regionName, spawns, fallback);
        }
    }

    /**
     * Retrieves spawnpoints for a region.
     */
    public List<Location> getSpawnPoints(String regionName) {
        List<Location> spawns = regionSpawnPoints.get(regionName);
        return (spawns == null) ? Collections.emptyList() : Collections.unmodifiableList(spawns);
    }

    /**
     * Sets (adds) a new spawn point to a region,
     * updates config and reloads data in memory.
     */
    public void setSpawnPoint(String regionName, Location loc) {
        World w = loc.getWorld();
        // Attempt to find a matching region from config
        ProtectedRegion pr = plugin.getRegionManager().getRegionForLocation(loc);
        if (pr == null || !pr.getId().equals(regionName)) {
            // Check if regionName is valid in config
            if (!regionSpawnPoints.containsKey(regionName)) {
                plugin.getLogger().warning("No such region in config: " + regionName);
                return;
            }
        }
        plugin.getConfigManager().addSpawnPoint(w, regionName, loc);
        // Refresh local cache
        reloadData();
        plugin.getLogger().info("Spawn point set for region " + regionName);
    }

    // ================== Mode-Specific Helpers ===================
    private Location getSequentialSpawn(String regionName, List<Location> spawns, Location fallback) {
        int idx = regionIndexes.getOrDefault(regionName, 0);
        if (idx >= spawns.size()) {
            idx = 0;
        }
        Location base = spawns.get(idx);
        regionIndexes.put(regionName, idx + 1);
        return applyOffset(base, fallback);
    }

    private Location getRandomSpawn(List<Location> spawns, Location fallback) {
        Location base = spawns.get(new Random().nextInt(spawns.size()));
        return applyOffset(base, fallback);
    }

    private Location getShuffleSpawn(String regionName, List<Location> spawns, Location fallback) {
        List<Location> shuffle = regionShuffleOrder.getOrDefault(regionName, new ArrayList<>());
        if (shuffle.isEmpty()) {
            // Rebuild shuffle list if empty
            regionShuffleOrder.put(regionName, new ArrayList<>(spawns));
            shuffle = regionShuffleOrder.get(regionName);
            Collections.shuffle(shuffle);
        }
        Location base = shuffle.remove(0);
        return applyOffset(base, fallback);
    }

    /**
     * Applies Y offset to the given spawn location, or returns fallback if null.
     */
    private Location applyOffset(Location base, Location fallback) {
        if (base == null) {
            return fallback;
        }
        return new Location(base.getWorld(),
                base.getX(),
                base.getY() + teleportYOffset,
                base.getZ(),
                base.getYaw(),
                base.getPitch());
    }
}
