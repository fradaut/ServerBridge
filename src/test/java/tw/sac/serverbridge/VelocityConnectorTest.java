package tw.sac.serverbridge;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VelocityConnectorTest {
    @Test
    void createsBungeeCordConnectMessage() throws IOException {
        byte[] message = VelocityConnector.connectMessage("resource");

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
            assertEquals("Connect", input.readUTF());
            assertEquals("resource", input.readUTF());
            assertEquals(0, input.available());
        }
    }
}
