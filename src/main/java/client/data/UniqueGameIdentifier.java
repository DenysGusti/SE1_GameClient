package client.data;

public record UniqueGameIdentifier(String uniqueGameID) {
    private static final int GAME_ID_LENGTH = 5;

    public UniqueGameIdentifier {
        if (uniqueGameID == null)
            throw new IllegalArgumentException("uniqueGameID is null");
        if (uniqueGameID.length() != GAME_ID_LENGTH)
            throw new IllegalArgumentException("uniqueGameID length is wrong");
    }
}
