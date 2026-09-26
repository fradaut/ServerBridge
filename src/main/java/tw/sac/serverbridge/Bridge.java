package tw.sac.serverbridge;

import org.bukkit.Location;

record Bridge(
        String id,
        String displayName,
        String world,
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ,
        ConnectionMode connectionMode,
        String host,
        int port,
        String proxyServer,
        Arrival arrival
) {
    boolean contains(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName().equals(world)
                && containsBlock(location.getBlockX(), minX, maxX)
                && containsBlock(location.getBlockY(), minY, maxY)
                && containsBlock(location.getBlockZ(), minZ, maxZ);
    }

    /**
     * Region corners are block coordinates. Comparing the player's precise
     * decimal position would make a one-block-wide region impossible to enter
     * (for example, block -13 is normally occupied at X=-12.5).
     */
    static boolean containsBlock(int playerBlock, double min, double max) {
        return playerBlock >= Math.floor(min) && playerBlock <= Math.floor(max);
    }

    record Arrival(String world, double x, double y, double z, float yaw, float pitch) {
    }

    enum ConnectionMode {
        NATIVE,
        WATERFALL
    }
}
