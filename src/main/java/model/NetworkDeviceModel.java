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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkDeviceModel that = (NetworkDeviceModel) o;

        if (!uuid.equals(that.uuid)) return false;
        if (type != that.type) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
