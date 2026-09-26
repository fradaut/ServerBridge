package tw.sac.serverbridge;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

final class BridgeConfig {
    private BridgeConfig() {
    }

    static Map<String, Bridge> load(ConfigurationSection root, Logger logger) {
        Map<String, Bridge> bridges = new LinkedHashMap<>();
        if (root == null) {
            return bridges;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null || !section.getBoolean("enabled", true)) {
                continue;
            }
            try {
                Bridge bridge = parse(id, section);
                bridges.put(id.toLowerCase(Locale.ROOT), bridge);
            } catch (IllegalArgumentException exception) {
                logger.warning("Ignoring bridge '" + id + "': " + exception.getMessage());
            }
        }
        return bridges;
    }

    private static Bridge parse(String id, ConfigurationSection section) {
        String world = required(section, "world");
        Bridge.ConnectionMode connectionMode = connectionMode(section);
        String host = "";
        int port = 0;
        String proxyServer = "";
        if (connectionMode == Bridge.ConnectionMode.NATIVE) {
            host = required(section, "destination.host");
            port = section.getInt("destination.port", 25565);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("destination.port must be between 1 and 65535");
            }
        } else {
            proxyServer = required(section, "destination.server");
        }

        double x1 = number(section, "region.min.x");
        double y1 = number(section, "region.min.y");
        double z1 = number(section, "region.min.z");
        double x2 = number(section, "region.max.x");
        double y2 = number(section, "region.max.y");
        double z2 = number(section, "region.max.z");

        Bridge.Arrival arrival = null;
        if (section.isConfigurationSection("destination.arrival")) {
            arrival = new Bridge.Arrival(
                    required(section, "destination.arrival.world"),
                    number(section, "destination.arrival.x"),
                    number(section, "destination.arrival.y"),
                    number(section, "destination.arrival.z"),
                    (float) section.getDouble("destination.arrival.yaw", 0.0),
                    (float) section.getDouble("destination.arrival.pitch", 0.0)
            );
        }

        return new Bridge(
                id,
                section.getString("display-name", id),
                world,
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2),
                connectionMode,
                host,
                port,
                proxyServer,
                arrival
        );
    }

    private static Bridge.ConnectionMode connectionMode(ConfigurationSection section) {
        String value = section.getString("destination.mode", "native").trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "native", "transfer" -> Bridge.ConnectionMode.NATIVE;
            case "velocity", "proxy" -> Bridge.ConnectionMode.VELOCITY;
            default -> throw new IllegalArgumentException(
                    "destination.mode must be 'native' or 'velocity', but was '" + value + "'"
            );
        };
    }

    private static String required(ConfigurationSection section, String path) {
        String value = section.getString(path);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(path + " is required");
        }
        return value.trim();
    }

    private static double number(ConfigurationSection section, String path) {
        if (!section.isDouble(path) && !section.isInt(path) && !section.isLong(path)) {
            throw new IllegalArgumentException(path + " must be a number");
        }
        return section.getDouble(path);
    }
}
