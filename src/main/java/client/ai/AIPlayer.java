package client.ai;

import client.ai.path.ExpectedArrivalPathFactory;
import client.ai.state.AIState;
import client.ai.state.ScoutingMySideState;
import client.ai.utilities.FullMapUtilities;
import client.data.XYPair;
import client.data.fromclient.EMove;
import client.data.fromserver.FullMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AIPlayer {
    private static final Logger logger = LoggerFactory.getLogger(AIPlayer.class);

    private final FullMapUtilities fullMapUtilities;
    private final ExpectedArrivalPathFactory expectedArrivalPathFactory;
    private final KnowledgeBase knowledgeBase;

    private AIState currentAIState = new ScoutingMySideState(this);
    private final Queue<XYPair> plannedStepPath = new ArrayDeque<>();

    public AIPlayer(FullMapUtilities fullMapUtilities, ExpectedArrivalPathFactory expectedArrivalPathFactory,
                    KnowledgeBase knowledgeBase) {
        if (fullMapUtilities == null)
            throw new IllegalArgumentException("fullMapUtilities is null");
        if (expectedArrivalPathFactory == null)
            throw new IllegalArgumentException("expectedArrivalPathFactory is null");
        if (knowledgeBase == null)
            throw new IllegalArgumentException("knowledgeBase is null");

        this.fullMapUtilities = fullMapUtilities;
        this.expectedArrivalPathFactory = expectedArrivalPathFactory;
        this.knowledgeBase = knowledgeBase;
    }

    public boolean isFirstValidEnemyPlayerPositionIdentified() {
        return knowledgeBase.isFirstValidEnemyPlayerPositionIdentified();
    }

    public boolean hasMoves() {
        return !plannedStepPath.isEmpty();
    }

    public void setAIState(AIState aiState) {
        if (aiState == null)
            throw new IllegalArgumentException("aiState is null");

        logger.info("State transition: {} -> {}", currentAIState.getClass().getSimpleName(), aiState.getClass().getSimpleName());
        currentAIState = aiState;

        logger.debug("Clearing step-path queue.");
        plannedStepPath.clear();
    }

    public void setPlannedStepPath(List<XYPair> plannedStepPath) {
        if (plannedStepPath == null)
            throw new IllegalArgumentException("plannedPath is null");
        if (!this.plannedStepPath.isEmpty())
            throw new IllegalStateException("plannedPath is not empty");

        this.plannedStepPath.addAll(plannedStepPath);
        logger.debug("New step-path set. Steps remaining: {}", this.plannedStepPath.size());
    }

    // Command
    public void updateKnowledgeBase(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        knowledgeBase.update(fullMap);
        currentAIState.handleFullMapUpdate(fullMap);

        XYPair currentMyPlayerPosition = knowledgeBase.getCurrentMyPlayerPosition();
        if (currentMyPlayerPosition.equals(plannedStepPath.element())) {
            plannedStepPath.remove();
            logger.debug("Remaining path nodes: {}. Reached node {}", plannedStepPath.size(), currentMyPlayerPosition);
        } else
            logger.debug("Remaining path nodes: {}", plannedStepPath.size());
    }

    // Query
    public EMove getNextMove() {
        XYPair currentMyPlayerPosition = knowledgeBase.getCurrentMyPlayerPosition();
        if (currentMyPlayerPosition == null)
            throw new IllegalStateException("currentMyPlayerPosition is null");

        XYPair nextPosition = plannedStepPath.element();
        var delta = new XYPair(nextPosition.x() - currentMyPlayerPosition.x(), nextPosition.y() - currentMyPlayerPosition.y());
        EMove move = EMove.fromDelta(delta);

        logger.info("Executing Move #{}: {} ({} -> {})", knowledgeBase.getMoveCounter(), move, currentMyPlayerPosition, nextPosition);
        return move;
    }

    public List<XYPair> getStepPathToTarget(XYPair target) {
        if (target == null)
            throw new IllegalArgumentException("target is null");

        return knowledgeBase.getStepPathToTarget(target);
    }

    public List<XYPair> getStepPathForScouting(FullMap fullMap, boolean onMySide) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        List<XYPair> unrevealedGrass = fullMapUtilities.getUnrevealedGrass(fullMap.nodes(), onMySide);
        if (!onMySide && knowledgeBase.isFirstValidEnemyPlayerPositionIdentified())
            unrevealedGrass = knowledgeBase.filterCoordinatesNearEnemyPlayer(unrevealedGrass);
        logger.debug("Collected {} unrevealed grass, onMySide: {}", unrevealedGrass.size(), onMySide);

        List<XYPair> mountains = fullMapUtilities.getMountains(fullMap.nodes(), onMySide);
        List<XYPair> neighborMountains = fullMapUtilities.getNeighbors(fullMap.size(), mountains, unrevealedGrass);
        var expectedArrivalPathSolver =
                expectedArrivalPathFactory.createSolver(fullMap.size(), knowledgeBase.getFullMapGraph(),
                        knowledgeBase.getCurrentMyPlayerPosition(), unrevealedGrass, neighborMountains);

        List<XYPair> waypoints = expectedArrivalPathSolver.getWaypoints();
        return knowledgeBase.getStepPathBetweenWaypoints(waypoints);
    }
}