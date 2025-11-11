package client.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerInformationTest {
    @Test
    @DisplayName("Negative Test: Constructor throws exception for null arguments")
    void NullNameOrAccount_ConstructPlayerInfo_ThrowsNullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        new PlayerInformation(null, "L", "U"), "First name null"),
                () -> assertThrows(NullPointerException.class, () ->
                        new PlayerInformation("F", null, "U"), "Last name null"),
                () -> assertThrows(NullPointerException.class, () ->
                        new PlayerInformation("F", "L", null), "UAccount null")
        );
    }
}