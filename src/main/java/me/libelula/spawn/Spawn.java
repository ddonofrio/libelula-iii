package me.libelula.spawn;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Spawn extends JavaPlugin {

    private WorldGuardPlugin worldGuard;
    private WorldEditPlugin worldEdit;
    private ConfigManager configManager;
    private PermissionManager permissionManager;
    private SpawnPointManager spawnPointManager;
    private RegionManager regionManager;
    private CommandManager commandManager;
    private SpawnSetup spawnSetup;


    @Override
    public void onEnable() {
        createPluginFolder();

        worldGuard = (WorldGuardPlugin) verifyDependency("WorldGuard", WorldGuardPlugin.class);
        worldEdit = (WorldEditPlugin) verifyDependency("WorldEdit", WorldEditPlugin.class);

        if (worldGuard == null || worldEdit == null) {
            getLogger().severe("Required dependencies not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Spawn Control plugin Started");

        configManager = new ConfigManager(this);
        permissionManager = new PermissionManager();
        configManager.loadConfig();
        spawnPointManager = new SpawnPointManager(this);
        regionManager = new RegionManager(this);
        commandManager = new CommandManager(this);
        spawnSetup = new SpawnSetup(this);

        getServer().getPluginManager().registerEvents(new EventManager(this), this);
    }

    @Override
    public void onDisable() {
        getConfigManager().saveConfig();
        getLogger().info("Spawn Control plugin Stopped.");
    }


    private void createPluginFolder() {
        File folder = getDataFolder();
        if (!folder.exists() && folder.mkdirs()) {
            getLogger().info("Plugin folder created: " + folder.getPath());
        }
    }

    private Plugin verifyDependency(String pluginName, Class<? extends Plugin> pluginClass) {
        Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
        if (pluginClass.isInstance(plugin)) {
            getLogger().info(pluginName + " detected.");
            return plugin;
        } else {
            getLogger().severe(pluginName + " not found!");
            return null;
        }
    }

    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public SpawnPointManager getSpawnPointManager() {
        return spawnPointManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public SpawnSetup getSpawnSetup() {
        return spawnSetup;
    }
}
