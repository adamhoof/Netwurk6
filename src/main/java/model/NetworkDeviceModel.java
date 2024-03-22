package model;

import common.NetworkDevice;
import common.NetworkDeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class NetworkDeviceModel implements NetworkDevice {
    protected UUID uuid;
    protected NetworkDeviceType type;
    protected MACAddress macAddress;

    protected List<NetworkConnection> networkConnections = new ArrayList<>();

    protected NetworkDeviceModel(UUID uuid, MACAddress macAddress, NetworkDeviceType type) {
        this.uuid = uuid;
        this.type = type;
        this.macAddress = macAddress;
    }

    public UUID getUuid() {
        return uuid;
    }

    public MACAddress getMacAddress() {
        return macAddress;
    }

    public NetworkDeviceType getNetworkDeviceType(){
        return type;
    }

    public void addNetworkConnection(NetworkConnection networkConnection){
        networkConnections.add(networkConnection);
    }
}
