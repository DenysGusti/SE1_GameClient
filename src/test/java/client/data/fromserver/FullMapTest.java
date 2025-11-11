package client.data.fromserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FullMapTest {
    @Test
    void constructor_nullArgs_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> new FullMap(null, null, null, null, null, null, null, null));
    }
}