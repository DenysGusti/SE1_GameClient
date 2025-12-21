package client.modelviewcontroller.model;

import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.observer.Publisher;
import client.modelviewcontroller.observer.Subscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MapModel {
    private static final Logger logger = LoggerFactory.getLogger(MapModel.class);

    private final Publisher<FullMap> onFullMapUpdated = new Publisher<>();
    private final Publisher<List<HalfMapGenerationException>> onHalfMapValidationErrors = new Publisher<>();
    private final Publisher<HalfMap> onHalfMapGenerated = new Publisher<>();

    public void subscribeOnFullMapUpdated(Subscriber<FullMap> view) {
        if (view == null)
            throw new IllegalArgumentException("view is null");

        onFullMapUpdated.subscribe(view);
    }

    public void updateFullMap(FullMap fullMap) {
        if (fullMap == null)
            throw new IllegalArgumentException("fullMap is null");

        onFullMapUpdated.notify(fullMap);
    }

    public void subscribeOnHalfMapValidationErrors(Subscriber<List<HalfMapGenerationException>> view) {
        if (view == null)
            throw new IllegalArgumentException("view is null");

        onHalfMapValidationErrors.subscribe(view);
    }

    public void updateHalfMapValidationErrors(List<HalfMapGenerationException> errors) {
        if (errors == null)
            throw new IllegalArgumentException("errors is null");

        onHalfMapValidationErrors.notify(errors);
    }

    public void subscribeOnHalfMapGenerated(Subscriber<HalfMap> view) {
        if (view == null)
            throw new IllegalArgumentException("view is null");

        onHalfMapGenerated.subscribe(view);
    }

    public void updateHalfMap(HalfMap halfMap) {
        if (halfMap == null)
            throw new IllegalArgumentException("halfMap is null");

        onHalfMapGenerated.notify(halfMap);
    }
}
