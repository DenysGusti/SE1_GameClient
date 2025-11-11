package client.data.fromserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FullMapTest {
    @Test
    @DisplayName("Negative Test: Constructor throws exception for null nodes")
    void NullNodes_ConstructFullMap_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new FullMap(null, null, null, null, null, null, null, null));
    }
}