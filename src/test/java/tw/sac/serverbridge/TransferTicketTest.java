package tw.sac.serverbridge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferTicketTest {
    @Test
    void roundTripsArrival() {
        Bridge.Arrival expected = new Bridge.Arrival("world_nether", 12.5, 70, -8.25, 90, -15);

        Bridge.Arrival actual = TransferTicket.decode(TransferTicket.encode(expected)).orElseThrow();

        assertEquals(expected, actual);
    }

    @Test
    void rejectsEmptyAndMalformedData() {
        assertTrue(TransferTicket.decode(new byte[0]).isEmpty());
        assertTrue(TransferTicket.decode(new byte[]{99, 1, 2, 3}).isEmpty());
    }
}
