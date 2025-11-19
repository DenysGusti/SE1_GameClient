package client.validation.rule;

import client.data.fromclient.HalfMap;
import client.validation.Notification;

public interface IHalfMapValidationRule {
    void validate(HalfMap halfMap, Notification notification);
}
