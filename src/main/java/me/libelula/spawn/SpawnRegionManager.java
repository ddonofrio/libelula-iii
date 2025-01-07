package me.libelula.spawn;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import java.util.List;

public class SpawnRegionManager {

    private final Spawn plugin;
    private final List<SpawnRegion> spawnRegions;

    // Constructor: caches spawn regions for fast lookups
    public SpawnRegionManager(Spawn plugin) {
        this.plugin = plugin;
        this.spawnRegions = plugin.getConfigManager().getSpawnRegions();
        plugin.getLogger().info("SpawnRegionManager initialized with "
                + spawnRegions.size() + " regions.");
    }

    /**
     * Returns the first ProtectedRegion that contains the given location,
     * or null if none contains it.
     */
    public ProtectedRegion getRegionForLocation(Location location) {
        if (location == null) {
            return null;
        }
        for (SpawnRegion sr : spawnRegions) {
            if (sr.getWorld().equals(location.getWorld())) {
                ProtectedRegion region = sr.getRegion();
                if (region != null
                        && region.contains(location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ())) {
                    return region;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the location is within any spawn region.
     */
    public boolean isLocationInSpawn(Location location) {
        return getRegionForLocation(location) != null;
    }
}
