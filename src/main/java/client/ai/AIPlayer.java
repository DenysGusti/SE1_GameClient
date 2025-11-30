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

        logger.info("AIPlayer initialized with strategy: {}", nodeTraversalStrategy.getClass().getSimpleName());
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

        logger.info("State Transition: {} -> {}", currentAIState.getClass().getSimpleName(), aiState.getClass().getSimpleName());
        currentAIState = aiState;

        plannedPath.clear();
        logger.debug("Cleared path queue.");
    }

    public void setPlannedPath(Collection<XYPair> plannedPath) {
        if (plannedPath == null)
            throw new IllegalArgumentException("plannedPath is null");

        this.plannedPath.addAll(plannedPath);
        logger.debug("New path set. Steps remaining: {}", this.plannedPath.size());
    }

    public List<XYPair> moveToTarget(XYPair target) {
        if (target == null)
            throw new IllegalArgumentException("target is null");

        logger.debug("Calculating path from {} to target {}", currentMyPlayerPosition, target);

        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(currentMyPlayerPosition, target);
        return allPaths.getFirst();
    }

    public Set<XYPair> collectUnrevealedGrassNodes(FullMap fullMap, boolean onMySide) {
        Set<XYPair> nodesToTraverse = fullMapService.getUnrevealedGrassNodes(fullMap, onMySide);
        Objects.requireNonNull(nodesToTraverse, "nodesToTraverse must not be null");

        int originalSize = nodesToTraverse.size();

        if (!onMySide && firstValidEnemyPlayerPosition != null) {
            nodesToTraverse = nodesToTraverse.stream()
                    .filter(coordinate -> fullMapGraph.getDistance(firstValidEnemyPlayerPosition, coordinate)
                            <= FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE)
                    .collect(Collectors.toSet());

            logger.debug("Filtered unrevealed nodes near enemy. Reduced from {} to {}", originalSize, nodesToTraverse.size());
        } else
            logger.debug("Collected {} unrevealed grass nodes, onMySide: {}", nodesToTraverse.size(), onMySide);

        return nodesToTraverse;
    }

    public List<XYPair> traverseUnrevealedGrassNodes(Set<XYPair> unrevealedGrassNodes) {
        if (unrevealedGrassNodes == null)
            throw new IllegalArgumentException("unrevealedGrassNodes is null");

        logger.debug("Ordering traversal for {} nodes using strategy...", unrevealedGrassNodes.size());

        List<XYPair> bypassOrder = nodeTraversalStrategy.orderNodes(currentMyPlayerPosition, unrevealedGrassNodes);
        Objects.requireNonNull(bypassOrder, "bypassOrder must not be null");

        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(bypassOrder);
        Objects.requireNonNull(allPaths, "allPaths must not be null");

        return allPaths.getFirst();
    }

    // Command
    public void updateKnowledgeBase(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        ++moveCounter;
        logger.debug("Processing Move #{}", moveCounter);

        currentMyPlayerPosition = fullMap.getOptionalMyPlayerPosition().orElseThrow();
        logger.debug("My Player Position updated: {}", currentMyPlayerPosition);

        if (moveCounter == FIRST_VALID_ENEMY_PLAYER_POSITION_MOVE) {
            firstValidEnemyPlayerPosition = fullMap.getOptionalEnemyPlayerPosition().orElseThrow();
            logger.info("ENEMY SPOTTED! First Valid Enemy Player Position: {}", firstValidEnemyPlayerPosition);
        }

        currentAIState.handleFullMapUpdate(fullMap);

        if (currentMyPlayerPosition.equals(plannedPath.element())) {
            plannedPath.remove();
            logger.debug("Reached node {}. Remaining steps: {}", currentMyPlayerPosition, plannedPath.size());
        }
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
            default -> throw new IllegalStateException(
                    String.format("Next path node %s is not adjacent to current %s", nextPosition, currentMyPlayerPosition)
            );
        };

        logger.info("Executing Move #{}: {} ({} -> {})", moveCounter, move, currentMyPlayerPosition, nextPosition);

        return move;
    }
}