package client.ai;

import client.ai.exception.TargetException;
import client.ai.graph.FullMapGraph;
import client.ai.state.AIState;
import client.ai.state.ScoutingMySideState;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AIPlayer {
    private static final Logger logger = LoggerFactory.getLogger(AIPlayer.class);
    private static final int FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE = 8;

    private final FullMapUtilities fullMapUtilities;
    private final ExpectedArrivalPathFactory expectedArrivalPathFactory;

    private FullMapGraph fullMapGraph = null;

    int moveCounter = 0;
    XYPair currentMyPlayerPosition = null;
    XYPair firstValidEnemyPlayerPosition = null;

    private AIState currentAIState = new ScoutingMySideState(this);
    private final Queue<XYPair> plannedStepPath = new ArrayDeque<>();

    public AIPlayer(FullMapUtilities fullMapUtilities, ExpectedArrivalPathFactory expectedArrivalPathFactory) {
        if (fullMapUtilities == null)
            throw new IllegalArgumentException("fullMapUtilities is null");
        if (expectedArrivalPathFactory == null)
            throw new IllegalArgumentException("expectedArrivalPathFactory is null");

        this.fullMapUtilities = fullMapUtilities;
        this.expectedArrivalPathFactory = expectedArrivalPathFactory;
    }

    public boolean isFullMapGraphInitialized() {
        return fullMapGraph != null;
    }

    public void setFullMapGraph(FullMapGraph fullMapGraph) {
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");

        this.fullMapGraph = fullMapGraph;
    }

    public boolean hasMoves() {
        return !plannedStepPath.isEmpty();
    }

    public boolean isFirstValidEnemyPlayerPositionIdentified() {
        return firstValidEnemyPlayerPosition != null;
    }

    // Command
    public void setAIState(AIState aiState) {
        if (aiState == null)
            throw new IllegalArgumentException("aiState is null");

        logger.info("State transition: {} -> {}", currentAIState.getClass().getSimpleName(), aiState.getClass().getSimpleName());
        currentAIState = aiState;

        plannedStepPath.clear();
        logger.debug("Cleared step-path queue.");
    }

    // Command
    public void setPlannedStepPath(List<XYPair> plannedStepPath) {
        if (plannedStepPath == null)
            throw new IllegalArgumentException("plannedPath is null");
        if (!this.plannedStepPath.isEmpty())
            throw new IllegalStateException("plannedPath is not empty");

        this.plannedStepPath.addAll(plannedStepPath);
        logger.debug("New step-path set. Steps remaining: {}", this.plannedStepPath.size());
    }

    // Query
    public List<XYPair> getStepPathToTarget(XYPair target) {
        if (target == null)
            throw new IllegalArgumentException("target is null");
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");
        if (fullMapGraph == null)
            throw new IllegalStateException("fullMapGraph is null");

        logger.debug("Calculating path from {} to target {}", currentMyPlayerPosition, target);
        List<XYPair> stepPath = fullMapGraph.getStepPathBetweenCoordinates(currentMyPlayerPosition, target);
        logger.trace("Step-path to target:\n{}", stepPath);
        return stepPath;
    }

    // Query
    public List<XYPair> getStepPathForScouting(FullMap fullMap, boolean onMySide) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMapGraph == null)
            throw new IllegalStateException("fullMapGraph is null");
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");

        logger.debug("Calculating path for scouting, on my side: {}", onMySide);

        List<XYPair> unrevealedGrassNodes = collectUnrevealedGrassNodes(fullMap, onMySide);
        Objects.requireNonNull(unrevealedGrassNodes, "unrevealedGrassNodes is null");
        if (unrevealedGrassNodes.isEmpty())
            throw new TargetException("unrevealedGrassNodes is empty");

        List<XYPair> neighborMountains = fullMapUtilities.getNeighborMountains(fullMap, unrevealedGrassNodes);
        Objects.requireNonNull(neighborMountains, "neighborMountains is null");

        ExpectedArrivalPathSolver expectedArrivalPathSolver = expectedArrivalPathFactory.createSolver(
                fullMap.size(), fullMapGraph, currentMyPlayerPosition, unrevealedGrassNodes, neighborMountains);

        List<XYPair> waypoints = expectedArrivalPathSolver.getWaypoints();
        Objects.requireNonNull(waypoints, "waypoints is null");

        List<XYPair> stepPath = fullMapGraph.getStepPathBetweenWaypoints(waypoints);
        logger.trace("Step-path for scouting:\n{}", stepPath);
        return stepPath;
    }

    // Command
    public void updateKnowledgeBase(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        ++moveCounter;

        currentMyPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();

        if (moveCounter == FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE) {
            firstValidEnemyPlayerPosition = fullMap.getOptionalEnemyPlayerPosition().orElseThrow();
            logger.info("ENEMY SPOTTED! First Valid Enemy Player Position: {}", firstValidEnemyPlayerPosition);
        }

        currentAIState.handleFullMapUpdate(fullMap);

        if (currentMyPlayerPosition.equals(plannedStepPath.element())) {
            plannedStepPath.remove();
            logger.debug("Remaining path nodes: {}. Reached node {}", plannedStepPath.size(), currentMyPlayerPosition);
        } else
            logger.debug("Remaining path nodes: {}", plannedStepPath.size());
    }

    // Query
    public EMove getNextMove() {
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");

        XYPair nextPosition = plannedStepPath.element();  // throws an exception if queue is empty
        Objects.requireNonNull(nextPosition, "nextPosition is null");

        var delta = new XYPair(
                nextPosition.x() - currentMyPlayerPosition.x(),
                nextPosition.y() - currentMyPlayerPosition.y()
        );

        EMove move = switch (delta) {
            case XYPair(int dx, int dy) when dx == 0 && dy == 1 -> EMove.Down;
            case XYPair(int dx, int dy) when dx == 0 && dy == -1 -> EMove.Up;
            case XYPair(int dx, int dy) when dx == 1 && dy == 0 -> EMove.Right;
            case XYPair(int dx, int dy) when dx == -1 && dy == 0 -> EMove.Left;
            default -> throw new IllegalStateException(
                    String.format("Next path node %s is not adjacent to current %s", nextPosition, currentMyPlayerPosition)
            );
        };

        logger.info("Executing Move #{}: {} ({} -> {})", moveCounter, move, currentMyPlayerPosition, nextPosition);

        return move;
    }

    private List<XYPair> collectUnrevealedGrassNodes(FullMap fullMap, boolean onMySide) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMapGraph == null)
            throw new IllegalStateException("fullMapGraph is null");

        List<XYPair> nodesToTraverse = fullMapUtilities.getUnrevealedGrassNodes(fullMap, onMySide);
        Objects.requireNonNull(nodesToTraverse, "nodesToTraverse is null");

        int originalSize = nodesToTraverse.size();

        if (!onMySide && firstValidEnemyPlayerPosition != null) {
            nodesToTraverse = nodesToTraverse.stream()
                    .filter(coordinate -> fullMapGraph.getDistance(firstValidEnemyPlayerPosition, coordinate)
                            <= FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE)
                    .toList();

            logger.debug("Filtered unrevealed nodes near enemy. Reduced from {} to {}", originalSize, nodesToTraverse.size());
        } else
            logger.debug("Collected {} unrevealed grass nodes, onMySide: {}", nodesToTraverse.size(), onMySide);

        return nodesToTraverse;
    }
}