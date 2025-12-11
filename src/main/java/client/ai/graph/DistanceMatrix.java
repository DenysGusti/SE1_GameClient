package client.ai.graph;

public record DistanceMatrix(byte[] data, int size) {
    public DistanceMatrix {
        if (data == null)
            throw new IllegalArgumentException("data is null");

        if (data.length != size * size)
            throw new IllegalArgumentException("data.length != size * size");
    }

    public int getDistance(int nodeFrom, int nodeTo) {
        return Byte.toUnsignedInt(data[nodeFrom * size + nodeTo]);
    }

    public int calculateTotalDistance(int[] path) {
        if (path == null)
            throw new IllegalArgumentException("path is null");

        int distance = 0;
        for (int i = 0; i < path.length - 1; ++i)
            distance += getDistance(path[i], path[i + 1]);
        return distance;
    }
}