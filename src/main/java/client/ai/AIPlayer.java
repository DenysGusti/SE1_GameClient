package client.ai;

import client.ai.state.AIState;
import client.ai.state.ScoutingMySideState;
import client.ai.tsp.NodeTraversalStrategy;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AIPlayer {
    private static final Logger logger = LoggerFactory.getLogger(AIPlayer.class);
    private static final int FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE = 8;

    private final FullMapService fullMapService;
    private final FullMapGraph fullMapGraph;
    private final NodeTraversalStrategy nodeTraversalStrategy;

    int moveCounter = 0;
    XYPair currentMyPlayerPosition = null;
    XYPair firstValidEnemyPlayerPosition = null;

    private AIState currentAIState = new ScoutingMySideState(this);
    private final Queue<XYPair> plannedPath = new ArrayDeque<>();

    public AIPlayer(FullMapService fullMapService, FullMapGraph fullMapGraph, NodeTraversalStrategy nodeTraversalStrategy) {
        if (fullMapService == null)
            throw new IllegalArgumentException("fullMapService is null");
        if (fullMapGraph == null)
            throw new IllegalArgumentException("fullMapGraph is null");
        if (nodeTraversalStrategy == null)
            throw new IllegalArgumentException("nodeTraversalStrategy is null");

        this.fullMapService = fullMapService;
        this.fullMapGraph = fullMapGraph;
        this.nodeTraversalStrategy = nodeTraversalStrategy;
    }

    public boolean hasMoves() {
        return !plannedPath.isEmpty();
    }

    public boolean isFirstValidEnemyPlayerPositionIdentified() {
        return firstValidEnemyPlayerPosition != null;
    }

    public void setAIState(AIState aiState) {
        if (aiState == null)
            throw new IllegalArgumentException("aiState is null");

        currentAIState = aiState;
        plannedPath.clear();
    }

    public void setPlannedPath(Collection<XYPair> plannedPath) {
        if (plannedPath == null)
            throw new IllegalArgumentException("plannedPath is null");

        this.plannedPath.addAll(plannedPath);
    }

    public List<XYPair> moveToTarget(XYPair target) {
        if (target == null)
            throw new IllegalArgumentException("target is null");

        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(currentMyPlayerPosition, target);
        return allPaths.getFirst();
    }

    public Set<XYPair> collectUnrevealedGrassNodes(FullMap fullMap, boolean onMySide) {
        Set<XYPair> nodesToTraverse = fullMapService.getUnrevealedGrassNodes(fullMap, onMySide);
        if (!onMySide && firstValidEnemyPlayerPosition != null)
            nodesToTraverse = nodesToTraverse.stream()
                    .filter(coordinate -> fullMapGraph.getDistance(firstValidEnemyPlayerPosition, coordinate)
                            <= FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE)
                    .collect(Collectors.toSet());
        return nodesToTraverse;
    }

    public List<XYPair> traverseUnrevealedGrassNodes(Set<XYPair> unrevealedGrassNodes) {
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");

        List<XYPair> bypassOrder = nodeTraversalStrategy.orderNodes(currentMyPlayerPosition, unrevealedGrassNodes);
        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(bypassOrder);
        return allPaths.getFirst();
    }

    // Command
    public void updateKnowledgeBase(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        ++moveCounter;

        currentMyPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();
        logger.debug("My Player Position: {}", currentMyPlayerPosition);

        if (moveCounter == FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE) {
            firstValidEnemyPlayerPosition = fullMap.getOptionalEnemyPlayerPosition().orElseThrow();
            logger.debug("First Valid Enemy Player Position: {}", firstValidEnemyPlayerPosition);
        }

        currentAIState.handleFullMapUpdate(fullMap);

        if (currentMyPlayerPosition.equals(plannedPath.element()))
            plannedPath.remove();

        logger.debug("Planned Path: {}", plannedPath);
    }

    // Query
    public EMove getNextMove() {
        Objects.requireNonNull(currentMyPlayerPosition, "currentMyPlayerPosition must not be null");

        XYPair nextPosition = plannedPath.element();  // throws an exception if queue is empty
        Objects.requireNonNull(nextPosition, "nextPosition must not be null");

        var delta = new XYPair(
                nextPosition.x() - currentMyPlayerPosition.x(),
                nextPosition.y() - currentMyPlayerPosition.y()
        );

        EMove move = switch (delta) {
            case XYPair(int dx, int dy) when dx == 0 && dy == 1 -> EMove.Down;
            case XYPair(int dx, int dy) when dx == 0 && dy == -1 -> EMove.Up;
            case XYPair(int dx, int dy) when dx == 1 && dy == 0 -> EMove.Right;
            case XYPair(int dx, int dy) when dx == -1 && dy == 0 -> EMove.Left;
            default -> throw new IllegalStateException("Next path node is not an adjacent neighbour: " + nextPosition);
        };

        logger.debug("Next move: {}", move);

        return move;
    }
}