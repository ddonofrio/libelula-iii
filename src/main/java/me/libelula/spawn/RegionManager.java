package me.libelula.spawn;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class RegionManager {

    private final Spawn plugin;

    public RegionManager(Spawn plugin) {
        this.plugin = plugin;
    }

    public boolean isLocationInSpawn(Location location) {
        World spawnWorld = plugin.getConfigManager().getSpawnWorld();
        ProtectedRegion spawnRegion = plugin.getConfigManager().getSpawnRegion();

        if (spawnWorld == null || spawnRegion == null) {
            return false;
        }

        if (!Objects.equals(location.getWorld(), spawnWorld)) {
            return false;
        }

        ApplicableRegionSet regionSet = com.sk89q.worldguard.WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery()
                .getApplicableRegions(BukkitAdapter.adapt(location));

        return regionSet.getRegions().stream().anyMatch(region -> region.getId().equalsIgnoreCase(spawnRegion.getId()));
    }
}
