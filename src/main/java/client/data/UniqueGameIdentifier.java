package client.data;

public record UniqueGameIdentifier(String uniqueGameID) {
    public UniqueGameIdentifier {
        if (uniqueGameID == null)
            throw new IllegalArgumentException("uniqueGameID is null");
    }
}
