package client.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerInformationTest {
    @Test
    void constructor_nullArgs_shouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> new PlayerInformation(null, "L", "U"));
        assertThrows(NullPointerException.class, () -> new PlayerInformation("F", null, "U"));
        assertThrows(NullPointerException.class, () -> new PlayerInformation("F", "L", null));
    }
}