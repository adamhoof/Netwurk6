package common;

import java.util.UUID;

public record NetworkCommunicationAnimationFinishedEvent(UUID communicationUuid) implements Event{
}
