package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record PlayerInformation(String firstName, String lastName, String uaccount) {
    private static final Logger logger = LoggerFactory.getLogger(PlayerInformation.class);

    public PlayerInformation {
        if (firstName == null)
            throw new IllegalArgumentException("firstName must not be null");
        if (lastName == null)
            throw new IllegalArgumentException("lastName must not be null");
        if (uaccount == null)
            throw new IllegalArgumentException("uaccount must not be null");
    }
}
