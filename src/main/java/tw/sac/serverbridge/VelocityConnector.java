package tw.sac.serverbridge;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/** Sends a Connect request through Velocity's BungeeCord-compatible channel. */
final class VelocityConnector {
    static final String CHANNEL = "BungeeCord";

    private VelocityConnector() {
    }

    static void connect(Plugin plugin, Player player, String serverName) {
        player.sendPluginMessage(plugin, CHANNEL, connectMessage(serverName));
    }

    static byte[] connectMessage(String serverName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeUTF("Connect");
                output.writeUTF(serverName);
            }
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create Velocity connect message", exception);
        }
    }
}
