package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class RouterInterface extends NetworkDeviceModel {
    IPAddress ipAddress;
    MACAddress macAddress;

    public RouterInterface(UUID uuid, IPAddress ipAddress, MACAddress macAddress) {
        super(uuid,macAddress, NetworkDeviceType.ROUTER_INTERFACE);
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }
}
