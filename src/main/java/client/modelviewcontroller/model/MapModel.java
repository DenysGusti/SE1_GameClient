package client.modelviewcontroller.model;

import client.data.fromclient.HalfMap;
import client.data.fromserver.FullMap;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;
import client.modelviewcontroller.observer.Publisher;
import client.modelviewcontroller.observer.Subscriber;

import java.util.Collection;

public class MapModel {
    private final Publisher<FullMap> onFullMapUpdated = new Publisher<>();
    private final Publisher<Collection<HalfMapGenerationException>> onHalfMapValidationErrors = new Publisher<>();
    private final Publisher<HalfMap> onHalfMapGenerated = new Publisher<>();

    public void subscribeOnFullMapUpdated(Subscriber<FullMap> view) {
        onFullMapUpdated.subscribe(view);
    }

    public void updateFullMap(FullMap fullMap) {
        onFullMapUpdated.notify(fullMap);
    }

    public void subscribeOnHalfMapValidationErrors(Subscriber<Collection<HalfMapGenerationException>> view) {
        onHalfMapValidationErrors.subscribe(view);
    }

    public void updateHalfMapValidationErrors(Collection<HalfMapGenerationException> errors) {
        onHalfMapValidationErrors.notify(errors);
    }

    public void subscribeOnHalfMapGenerated(Subscriber<HalfMap> view) {
        onHalfMapGenerated.subscribe(view);
    }

    public void updateHalfMap(HalfMap halfMap) {
        onHalfMapGenerated.notify(halfMap);
    }
}
