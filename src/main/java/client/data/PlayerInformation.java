package client.data;

import java.util.Objects;

public record PlayerInformation(String firstName, String lastName, String uaccount) {
    public PlayerInformation {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName, "lastName must not be null");
        Objects.requireNonNull(uaccount, "uaccount must not be null");
    }
}
