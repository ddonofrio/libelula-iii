package me.libelula.spawn;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class SpawnRegion {

    private final World world;
    private final ProtectedRegion region;
    private final List<Location> spawnPoints;

    public SpawnRegion(World world, ProtectedRegion region, List<Location> spawnPoints) {
        this.world = world;
        this.region = region;
        this.spawnPoints = (spawnPoints == null) ? new ArrayList<>() : new ArrayList<>(spawnPoints);
    }

    public World getWorld() {
        return world;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public List<Location> getSpawnPoints() {
        return new ArrayList<>(spawnPoints);
    }

    public void setSpawnPoints(List<Location> points) {
        spawnPoints.clear();
        if (points != null) {
            for (Location loc : points) {
                if (loc.getWorld().equals(world)) {
                    spawnPoints.add(loc);
                } else {
                    throw new IllegalArgumentException(
                            "Location world doesn't match region's world.");
                }
            }
        }
    }

    public void addSpawnPoint(Location location) {
        if (location.getWorld().equals(world)) {
            spawnPoints.add(location);
        } else {
            throw new IllegalArgumentException("Location world doesn't match region's world.");
        }
    }

    public void clearSpawnPoints() {
        spawnPoints.clear();
    }

    public boolean hasSpawnPoints() {
        return !spawnPoints.isEmpty();
    }
}
