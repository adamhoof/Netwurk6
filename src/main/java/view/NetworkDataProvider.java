package view;

import java.util.List;

public class NetworkDataProvider {
    private final List<NetworkDeviceView> deviceViews;
    private final List<ConnectionLine> connectionLines;

    public NetworkDataProvider(List<NetworkDeviceView> deviceViews, List<ConnectionLine> connectionLines) {
        this.deviceViews = deviceViews;
        this.connectionLines = connectionLines;
    }

    public List<NetworkDeviceView> getDevices() {
        return deviceViews;
    }

    public List<ConnectionLine> getConnectionLines() {
        return connectionLines;
    }

    public void addDevice(NetworkDeviceView networkDeviceView) {
        deviceViews.add(networkDeviceView);
    }
}
