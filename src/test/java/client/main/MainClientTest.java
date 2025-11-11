package client.main;

import client.main.exception.CommandLineArgumentsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainClientTest {

    @Test
    @DisplayName("validateArguments should pass for 3 valid arguments")
    void validateArguments_valid3Args_shouldPass() {
        String[] args = {"TR", "server.com", "game-id"};
        assertDoesNotThrow(() -> MainClient.validateArguments(args));
    }

    @Test
    @DisplayName("validateArguments should throw for wrong number of arguments")
    void validateArguments_invalidArgCount_shouldThrow() {
        String[] args1 = {"TR"};
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(args1));
    }

    @Test
    @DisplayName("validateArguments should throw for invalid game mode")
    void validateArguments_invalidGameMode_shouldThrow() {
        String[] args = {"GUI", "server.com", "game-id"};
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(args));
    }
}