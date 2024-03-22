package common;

import view.NetworkDeviceView;

public interface Connection {
    public NetworkDeviceView getStartDevice();

    public NetworkDeviceView getEndDevice();
}
