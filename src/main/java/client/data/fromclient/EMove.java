package client.data.fromclient;

import client.data.XYPair;

public enum EMove {
    Up,
    Left,
    Right,
    Down;

    public static EMove fromDelta(XYPair delta) {
        if (delta == null)
            throw new IllegalArgumentException("delta is null");

        return switch (delta) {
            case XYPair(int dx, int dy) when dx == 0 && dy == -1 -> Up;
            case XYPair(int dx, int dy) when dx == 0 && dy == 1 -> Down;
            case XYPair(int dx, int dy) when dx == -1 && dy == 0 -> Left;
            case XYPair(int dx, int dy) when dx == 1 && dy == 0 -> Right;
            default -> throw new IllegalStateException("Invalid move delta: " + delta);
        };
    }
}
