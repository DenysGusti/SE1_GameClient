package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record UniquePlayerIdentifier(String uniquePlayerID) {
    private static final Logger logger = LoggerFactory.getLogger(UniquePlayerIdentifier.class);

    public UniquePlayerIdentifier {
        if (uniquePlayerID == null)
            throw new IllegalArgumentException("uniquePlayerID is null");
    }
}
