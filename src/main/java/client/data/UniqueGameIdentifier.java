package client.data;

public record UniqueGameIdentifier(String uniqueGameID) {
    public UniqueGameIdentifier {
        if (uniqueGameID == null)
            throw new IllegalArgumentException("uniqueGameID is null");
        if (uniqueGameID.length() != 5)
            throw new IllegalArgumentException("uniqueGameID length is not 5");
    }
}
