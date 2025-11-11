package client.data.fromclient;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HalfMapTest {
    @Test
    void constructor_nullArgs_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> new HalfMap(null, Set.of()));
        assertThrows(NullPointerException.class, () -> new HalfMap(Map.of(), null));
    }
}