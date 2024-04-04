package model;

import common.NetworkDevice;
import common.NetworkDeviceType;

import java.util.HashSet;
import java.util.UUID;

public abstract class NetworkDeviceModel implements NetworkDevice {
    protected UUID uuid;
    protected NetworkDeviceType type;
    protected MACAddress macAddress;

    protected String name;

    protected final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();

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

    public NetworkDeviceType getNetworkDeviceType() {
        return type;
    }

    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        directConnections.add(networkDeviceModel);
        return true;
    }

}
