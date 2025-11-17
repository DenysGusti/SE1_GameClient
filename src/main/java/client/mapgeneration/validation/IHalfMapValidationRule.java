package client.mapgeneration.validation;

import client.data.fromclient.HalfMap;

public interface IHalfMapValidationRule {
    boolean isValid(HalfMap halfMap);
}
