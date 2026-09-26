package tw.sac.serverbridge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeTest {
    @Test
    void oneBlockWideRegionIncludesTheWholeBlock() {
        assertTrue(Bridge.containsBlock(-13, -13, -13));
        assertFalse(Bridge.containsBlock(-12, -13, -13));
    }

    @Test
    void negativeCornerCoordinatesAreInclusive() {
        assertTrue(Bridge.containsBlock(-41, -41, -40));
        assertTrue(Bridge.containsBlock(-40, -41, -40));
        assertFalse(Bridge.containsBlock(-39, -41, -40));
    }
}
