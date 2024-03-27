package model;

import common.NetworkDeviceType;

import java.util.UUID;

public class RouterModel extends NetworkDeviceModel {
    private final RoutingTable routingTable;
    public RouterModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER);
        this.routingTable = new RoutingTable();
    }

    public void appendRoutingTable(RouteEntry routeEntry) {
        routingTable.addEntry(routeEntry);
    }

    public RoutingTable getRoutingTable(){
        return routingTable;
    }
}