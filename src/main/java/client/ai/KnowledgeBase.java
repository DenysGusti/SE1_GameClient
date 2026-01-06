package client.ai;

import client.ai.graph.FullMapGraph;
import client.data.XYPair;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KnowledgeBase {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBase.class);
    private static final int FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE = 8;

    private int moveCounter = 0;
    private XYPair currentMyPlayerPosition = null;
    private XYPair firstValidEnemyPlayerPosition = null;
    private final FullMapGraph fullMapGraph;

    public KnowledgeBase(FullMapGraph fullMapGraph) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");

        this.fullMapGraph = fullMapGraph;
    }

    public void update(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        ++moveCounter;
        currentMyPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();

        if (moveCounter == FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE) {
            firstValidEnemyPlayerPosition = fullMap.getOptionalEnemyPlayerPosition().orElseThrow();
            logger.info("ENEMY SPOTTED! First Valid Enemy Player Position: {}", firstValidEnemyPlayerPosition);
        }
    }

    public FullMapGraph getFullMapGraph() {
        return fullMapGraph;
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public XYPair getCurrentMyPlayerPosition() {
        return currentMyPlayerPosition;
    }

    public boolean isFirstValidEnemyPlayerPositionIdentified() {
        return firstValidEnemyPlayerPosition != null;
    }

    public List<XYPair> getStepPathToTarget(XYPair target) {
        if (target == null)
            throw new IllegalArgumentException("target is null");
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");

        return fullMapGraph.getStepPathBetweenCoordinates(currentMyPlayerPosition, target);
    }

    public List<XYPair> filterCoordinatesNearEnemyPlayer(List<XYPair> coordinates) {
        if (coordinates == null)
            throw new IllegalArgumentException("coordinates is null");
        if (firstValidEnemyPlayerPosition == null)
            throw new IllegalStateException("firstValidEnemyPlayerPosition is null");

        List<XYPair> filteredCoordinates = coordinates.stream()
                .filter(coordinate ->
                        fullMapGraph.getDistance(firstValidEnemyPlayerPosition, coordinate) <= FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE)
                .toList();
        logger.debug("Filtered coordinates near enemy player. Reduced from {} to {}", coordinates, filteredCoordinates.size());
        return filteredCoordinates;
    }

    public List<XYPair> getStepPathBetweenWaypoints(List<XYPair> waypoints) {
        if (waypoints == null)
            throw new IllegalArgumentException("waypoints is null");
        if (waypoints.size() <= 1)
            throw new IllegalArgumentException("waypoint must have at least start and end");

        return fullMapGraph.getStepPathBetweenWaypoints(waypoints);
    }
}