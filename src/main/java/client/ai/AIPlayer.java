package client.ai;

import client.ai.exception.TargetException;
import client.ai.graph.FullMapGraph;
import client.ai.mountain.MountainSelector;
import client.ai.mountain.StepPathMetric;
import client.ai.state.AIState;
import client.ai.state.ScoutingMySideState;
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

    private final FullMapSplitter fullMapSplitter;
    private final MountainSelector mountainSelector;

    private FullMapGraph fullMapGraph = null;

    int moveCounter = 0;
    XYPair currentMyPlayerPosition = null;
    XYPair firstValidEnemyPlayerPosition = null;

    private AIState currentAIState = new ScoutingMySideState(this);
    private final Queue<XYPair> plannedPath = new ArrayDeque<>();

    public AIPlayer(FullMapSplitter fullMapSplitter, MountainSelector mountainSelector) {
        if (fullMapSplitter == null)
            throw new IllegalArgumentException("fullMapSplitter is null");
        if (mountainSelector == null)
            throw new IllegalArgumentException("mountainSelector is null");

        this.fullMapSplitter = fullMapSplitter;
        this.mountainSelector = mountainSelector;
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
        return !plannedPath.isEmpty();
    }

    public boolean isFirstValidEnemyPlayerPositionIdentified() {
        return firstValidEnemyPlayerPosition != null;
    }

    // Command
    public void setAIState(AIState aiState) {
        if (aiState == null)
            throw new IllegalArgumentException("aiState is null");

        logger.info("State Transition: {} -> {}", currentAIState.getClass().getSimpleName(), aiState.getClass().getSimpleName());
        currentAIState = aiState;

        plannedPath.clear();
        logger.debug("Cleared path queue.");
    }

    // Command
    public void setPlannedPath(List<XYPair> plannedPath) {
        if (plannedPath == null)
            throw new IllegalArgumentException("plannedPath is null");
        if (!this.plannedPath.isEmpty())
            throw new IllegalStateException("plannedPath is not empty");

        this.plannedPath.addAll(plannedPath);
        logger.debug("New path set. Steps remaining: {}", this.plannedPath.size());
    }

    // Query
    public List<XYPair> getStepPathToGoal(XYPair goal) {
        if (goal == null)
            throw new IllegalArgumentException("target is null");
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");
        if (fullMapGraph == null)
            throw new IllegalStateException("fullMapGraph is null");

        logger.debug("Calculating path from {} to target {}", currentMyPlayerPosition, goal);

        List<List<XYPair>> allPaths = fullMapGraph.getAllPaths(currentMyPlayerPosition, goal);
        return allPaths.getFirst();
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

        Set<XYPair> unrevealedGrassNodes = collectUnrevealedGrassNodes(fullMap, onMySide);
        if (unrevealedGrassNodes.isEmpty())
            throw new TargetException("unrevealedGrassNodes is empty");

        StepPathMetric stepPathMetric = mountainSelector.selectMountainPath(fullMapGraph, fullMap, unrevealedGrassNodes);
        logger.trace("Step-path: {}", stepPathMetric.path());
        return stepPathMetric.path();
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

        if (currentMyPlayerPosition.equals(plannedPath.element())) {
            plannedPath.remove();
            logger.debug("Remaining path nodes: {}. Reached node {}", plannedPath.size(), currentMyPlayerPosition);
        } else
            logger.debug("Remaining path nodes: {}", plannedPath.size());
    }

    // Query
    public EMove getNextMove() {
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");

        XYPair nextPosition = plannedPath.element();  // throws an exception if queue is empty
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

    private Set<XYPair> collectUnrevealedGrassNodes(FullMap fullMap, boolean onMySide) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");
        if (fullMapGraph == null)
            throw new IllegalStateException("fullMapGraph is null");

        Set<XYPair> nodesToTraverse = fullMapSplitter.getUnrevealedGrassNodes(fullMap, onMySide);
        Objects.requireNonNull(nodesToTraverse, "nodesToTraverse is null");

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
}