package view;

import java.util.ArrayList;
import java.util.List;

public class NetworkDeviceViewProvider {
    private final List<NetworkDeviceView> deviceViews;

    public NetworkDeviceViewProvider() {
        deviceViews = new ArrayList<>();
    }

    public List<NetworkDeviceView> getDevices() {
        return deviceViews;
    }

    public void addDevice(NetworkDeviceView networkDeviceView) {
        deviceViews.add(networkDeviceView);
    }
}
