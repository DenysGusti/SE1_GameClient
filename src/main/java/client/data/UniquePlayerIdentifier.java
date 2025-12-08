package client.data;

public record UniquePlayerIdentifier(String uniquePlayerID) {
    public UniquePlayerIdentifier {
        if (uniquePlayerID == null)
            throw new IllegalArgumentException("uniquePlayerID is null");
    }
}
