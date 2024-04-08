package model;

import common.Connection;

public class NetworkConnection implements Connection {

    private final NetworkDeviceModel startDevice;
    private final NetworkDeviceModel endDevice;

    public NetworkConnection(NetworkDeviceModel first, NetworkDeviceModel second){
        startDevice = first;
        endDevice = second;
    }
    @Override
    public NetworkDeviceModel getStartDevice() {
        return startDevice;
    }

    @Override
    public NetworkDeviceModel getEndDevice() {
        return endDevice;
    }
}
