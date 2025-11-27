package client.ai;

import client.data.XYPair;

import java.util.List;
import java.util.Set;

public interface NodeTraversalStrategy {
    List<XYPair> orderNodes(XYPair start, Set<XYPair> nodes);
}
