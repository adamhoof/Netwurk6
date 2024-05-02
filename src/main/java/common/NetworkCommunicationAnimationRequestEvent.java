package common;

import javafx.animation.PathTransition;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.util.UUID;

public record NetworkCommunicationAnimationRequestEvent(UUID communicationId, Pair<PathTransition, Rectangle> frameThroughNetworkConnection) implements Event {
}
