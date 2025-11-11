package client.main;

import client.main.exception.CommandLineArgumentsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainClientTest {

    @Test
    @DisplayName("Valid arguments with gameID should pass")
    void Valid3Arguments_ValidateArguments_DoesNotThrow() {
        String[] args = {"TR", "server.com", "game-id"};
        assertDoesNotThrow(() -> MainClient.validateArguments(args));
    }

    @Test
    @DisplayName("Valid arguments without gameID should pass")
    void Valid2Arguments_ValidateArguments_DoesNotThrow() {
        String[] args = {"TR", "server.com"};
        assertDoesNotThrow(() -> MainClient.validateArguments(args));
    }

    @Test
    @DisplayName("Negative Test: Too few arguments should throw exception")
    void InvalidArgumentCount_ValidateArguments_ThrowsCommandLineArgumentsException() {
        String[] args1 = {"TR"};
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(args1));
    }

    @Test
    @DisplayName("Negative Test: Invalid game mode should throw exception")
    void InvalidGameMode_ValidateArguments_ThrowsCommandLineArgumentsException() {
        String[] args = {"GUI", "server.com", "game-id"};
        assertThrows(CommandLineArgumentsException.class, () -> MainClient.validateArguments(args));
    }
}