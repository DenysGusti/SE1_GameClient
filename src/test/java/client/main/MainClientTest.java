package client.main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import client.main.exception.CommandLineArgumentsException;

class MainClientTest {
    @Test
    void validateArguments_Null_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> MainClient.validateArguments(null));
    }

    @Test
    void validateArguments_InvalidLength_ThrowsCommandLineArgumentsException() {
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(new String[]{"GUI"}));
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(new String[]{"GUI", "url", "id", "extra"}));
    }

    @Test
    void validateArguments_InvalidMode_ThrowsCommandLineArgumentsException() {
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(new String[]{"CLI", "url"}));
    }

    @Test
    void validateArguments_ValidModes_DoesNotThrow() {
        assertDoesNotThrow(() -> MainClient.validateArguments(new String[]{"GUI", "url"}));
        assertDoesNotThrow(() -> MainClient.validateArguments(new String[]{"TR", "url", "id"}));
    }
}