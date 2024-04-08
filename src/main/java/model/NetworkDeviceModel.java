package model;

import common.NetworkDevice;
import common.NetworkDeviceType;

import java.util.UUID;

public abstract class NetworkDeviceModel implements NetworkDevice {
    protected UUID uuid;
    protected NetworkDeviceType type;
    protected MACAddress macAddress;

    protected String name;

    protected NetworkDeviceModel(UUID uuid, MACAddress macAddress, NetworkDeviceType type) {
        this.uuid = uuid;
        this.type = type;
        this.macAddress = macAddress;
    }

    protected NetworkDeviceModel(UUID uuid, MACAddress macAddress, NetworkDeviceType type, String name) {
        this.uuid = uuid;
        this.type = type;
        this.macAddress = macAddress;
        this.name = name;
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

    public abstract boolean addConnection(NetworkDeviceModel networkDeviceModel);

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override public String getName(){return name;}

    @Override
    public String toString() {
        return this.name;
    }
}
