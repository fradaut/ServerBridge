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
        String host,
        int port,
        Arrival arrival
) {
    boolean contains(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName().equals(world)
                && location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    record Arrival(String world, double x, double y, double z, float yaw, float pitch) {
    }
}
