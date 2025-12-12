package client.halfmaplogic.validation.rule;

import client.data.fromclient.HalfMap;
import client.halfmaplogic.validation.exception.HalfMapGenerationException;

import java.util.List;

public interface HalfMapValidationRule {
    List<HalfMapGenerationException> validate(HalfMap halfMap);
}
