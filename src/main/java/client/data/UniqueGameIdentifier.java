package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UniqueGameIdentifier(String uniqueGameID) {
    private static final Logger logger = LoggerFactory.getLogger(UniqueGameIdentifier.class);

    public UniqueGameIdentifier {
        if (uniqueGameID == null)
            throw new IllegalArgumentException("uniqueGameID must not be null");
    }
}
