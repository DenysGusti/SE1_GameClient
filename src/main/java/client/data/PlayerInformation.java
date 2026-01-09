package client.data;

public record PlayerInformation(String firstName, String lastName, String uAccount) {
    private static final int MAX_STRING_LENGTH = 50;

    public PlayerInformation {
        if (firstName == null)
            throw new IllegalArgumentException("firstName is null");
        if (lastName == null)
            throw new IllegalArgumentException("lastName is null");
        if (uAccount == null)
            throw new IllegalArgumentException("uAccount is null");

        if (firstName.isEmpty())
            throw new IllegalArgumentException("firstName is empty");
        if (lastName.isEmpty())
            throw new IllegalArgumentException("lastName is empty");
        if (uAccount.isEmpty())
            throw new IllegalArgumentException("uAccount is empty");

        if (firstName.length() > MAX_STRING_LENGTH)
            throw new IllegalArgumentException("firstName length is too long");
        if (lastName.length() > MAX_STRING_LENGTH)
            throw new IllegalArgumentException("lastName length is too long");
        if (uAccount.length() > MAX_STRING_LENGTH)
            throw new IllegalArgumentException("uAccount length is too long");
    }
}
