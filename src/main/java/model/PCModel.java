package model;


import common.NetworkDeviceType;

import java.util.UUID;

public class PCModel extends NetworkDeviceModel {
    private IPAddress ipAddress;
    private RoutingTable routingTable;

    public PCModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.PC);
        this.routingTable = new RoutingTable();
    }
}