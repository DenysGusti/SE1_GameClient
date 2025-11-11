package client.data.fromclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HalfMapTest {
    @Test
    @DisplayName("Negative Test: Constructor throws exception for null collections")
    void NullNodesOrForts_ConstructHalfMap_ThrowsNullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        new HalfMap(null, Set.of()), "Nodes map cannot be null"),
                () -> assertThrows(NullPointerException.class, () ->
                        new HalfMap(Map.of(), null), "PotentialForts set cannot be null")
        );
    }
}