package me.libelula.spawn;

import org.bukkit.entity.Player;

public class PermissionManager {

    public static final String BYPASS_PERMISSION = "spawn.bypass";
    public static final String ADMIN_PERMISSION = "spawn.admin";

    public boolean hasBypassPermission(Player player) {
        return player.hasPermission(BYPASS_PERMISSION);
    }

    public boolean isAdmin(Player player) {
        return player.hasPermission(ADMIN_PERMISSION);
    }

}
