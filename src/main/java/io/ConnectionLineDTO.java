package io;

import java.util.UUID;

public record ConnectionLineDTO(UUID startDeviceId, UUID endDeviceId, String middleLabelText, String startLabelText,
                                String endLabelText, double startX, double startY, double endX, double endY) {
}
