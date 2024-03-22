package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class RouterModel extends NetworkDeviceModel {
    private IPAddress ipAddress;
    private RoutingTable routingTable;

    public RouterModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress,NetworkDeviceType.ROUTER);
        this.routingTable = new RoutingTable();
    }
}