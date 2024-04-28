package io;

import java.util.UUID;

public record ConnectionLineDTO(UUID startDeviceId, UUID endDeviceId) {
}
