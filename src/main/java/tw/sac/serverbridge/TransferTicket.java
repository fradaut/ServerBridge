package tw.sac.serverbridge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

final class TransferTicket {
    private static final int VERSION = 1;
    private static final int MAX_WORLD_NAME_BYTES = 256;

    private TransferTicket() {
    }

    static byte[] encode(Bridge.Arrival arrival) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeByte(VERSION);
                output.writeUTF(arrival.world());
                output.writeDouble(arrival.x());
                output.writeDouble(arrival.y());
                output.writeDouble(arrival.z());
                output.writeFloat(arrival.yaw());
                output.writeFloat(arrival.pitch());
            }
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not encode transfer destination", exception);
        }
    }

    static Optional<Bridge.Arrival> decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > 1024) {
            return Optional.empty();
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readUnsignedByte() != VERSION) {
                return Optional.empty();
            }
            String world = input.readUTF();
            if (world.isBlank() || world.getBytes(StandardCharsets.UTF_8).length > MAX_WORLD_NAME_BYTES) {
                return Optional.empty();
            }
            Bridge.Arrival arrival = new Bridge.Arrival(
                    world,
                    input.readDouble(), input.readDouble(), input.readDouble(),
                    input.readFloat(), input.readFloat()
            );
            if (!Double.isFinite(arrival.x()) || !Double.isFinite(arrival.y()) || !Double.isFinite(arrival.z())
                    || !Float.isFinite(arrival.yaw()) || !Float.isFinite(arrival.pitch())) {
                return Optional.empty();
            }
            return Optional.of(arrival);
        } catch (IOException exception) {
            return Optional.empty();
        }
    }
}
