package model;

public class SwitchConnection {
    NetworkDeviceModel networkDeviceModel;
    int port;

    public SwitchConnection(NetworkDeviceModel networkDeviceModel, int port) {
        this.networkDeviceModel = networkDeviceModel;
        this.port = port;
    }

    public NetworkDeviceModel getNetworkDeviceModel() {
        return networkDeviceModel;
    }

    public int getPort() {
        return port;
    }
}
