package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record UniquePlayerIdentifier(String uniquePlayerID) {
    private static final Logger logger = LoggerFactory.getLogger(UniquePlayerIdentifier.class);

    public UniquePlayerIdentifier {
        Objects.requireNonNull(uniquePlayerID, "uniqueGameID must not be null");
    }
}
