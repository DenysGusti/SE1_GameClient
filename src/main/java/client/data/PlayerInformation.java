package client.data;

public record PlayerInformation(String firstName, String lastName, String uAccount) {
    public PlayerInformation {
        if (firstName == null)
            throw new IllegalArgumentException("firstName must not be null");
        if (lastName == null)
            throw new IllegalArgumentException("lastName must not be null");
        if (uAccount == null)
            throw new IllegalArgumentException("uAccount must not be null");
    }
}
