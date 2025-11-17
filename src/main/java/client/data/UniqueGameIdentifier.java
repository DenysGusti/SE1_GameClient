package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record UniqueGameIdentifier(String uniqueGameID) {
    private static final Logger logger = LoggerFactory.getLogger(UniqueGameIdentifier.class);

    public UniqueGameIdentifier {
        Objects.requireNonNull(uniqueGameID, "uniqueGameID must not be null");
    }
}
