package common;

import java.util.UUID;

public interface NetworkDevice {
    UUID getUuid();
    NetworkDeviceType getNetworkDeviceType();
}
