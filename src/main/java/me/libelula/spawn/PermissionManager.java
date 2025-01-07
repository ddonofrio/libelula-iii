package me.libelula.spawn;

import org.bukkit.entity.Player;

public class PermissionManager {

    public static final String BYPASS_PERMISSION = "spawn.bypass";
    public static final String ADMIN_PERMISSION = "spawn.admin";
    public static final String DELAY_BYPASS_PERMISSION = "spawn.bypass.delay";
    public static final String SPAWN_PERMISSION_BASE = "spawn.player.use.spawn.";

    public boolean hasBypassPermission(Player player) {
        return player.hasPermission(BYPASS_PERMISSION);
    }

    public boolean isAdmin(Player player) {
        return player.hasPermission(ADMIN_PERMISSION);
    }

    public boolean hasDelayBypassPermission(Player player) {
        return player.hasPermission(DELAY_BYPASS_PERMISSION);
    }

    public int getHighestSpawnPermission(Player player) {
        for (int i = 9; i >= 0; i--) {
            if (player.hasPermission(SPAWN_PERMISSION_BASE + i)) {
                return i;
            }
        }
        return -1;
    }
}
