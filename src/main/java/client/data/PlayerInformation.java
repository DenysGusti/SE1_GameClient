package client.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public record PlayerInformation(String firstName, String lastName, String uaccount) {
    private static final Logger logger = LoggerFactory.getLogger(PlayerInformation.class);

    public PlayerInformation {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(uaccount, "uaccount must not be null");
    }
}
