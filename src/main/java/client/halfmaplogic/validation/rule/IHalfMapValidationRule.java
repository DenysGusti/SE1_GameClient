package client.halfmaplogic.validation.rule;

import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.Notification;

public interface IHalfMapValidationRule {
    void validate(HalfMap halfMap, Notification notification);
}
