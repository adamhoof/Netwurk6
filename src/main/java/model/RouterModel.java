package model;

import common.NetworkDeviceType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RouterModel extends NetworkDeviceModel {
    private final RoutingTable routingTable;

    private final Map<Network, IPAddress> ipAddressInNetwork = new HashMap<>();

    public RouterModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER);
        this.routingTable = new RoutingTable();
    }

    public void appendRoutingTable(RouteEntry routeEntry) {
        routingTable.addEntry(routeEntry);
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    private void processReceivedEntry(RouteEntry receivedEntry, IPAddress sourceIPAddress) {
        boolean routeExists = false;
        for (RouteEntry existingEntry : routingTable.getEntries()) {
            if (existingEntry.getDestinationNetwork().equals(receivedEntry.getDestinationNetwork())) {
                routeExists = true;
                if (receivedEntry.getHopCount() + 1 < existingEntry.getHopCount()) {
                    existingEntry.setHopCount(receivedEntry.getHopCount() + 1);
                    existingEntry.setNextHop(sourceIPAddress);
                }
                break;
            }
        }
        if (!routeExists) {
            routingTable.addEntry(new RouteEntry(receivedEntry.getDestinationNetwork(), sourceIPAddress, receivedEntry.getHopCount() + 1));
        }
    }

    public void receiveRoutingTable(RoutingTable receivedRoutingTable, IPAddress sourceIPAddress) {
        for (RouteEntry entry : receivedRoutingTable.getEntries()) {
            processReceivedEntry(entry, sourceIPAddress);
        }
    }

    public IPAddress ipAddressInNetwork(Network network) {
        return ipAddressInNetwork.get(network);
    }

    public boolean isInNetwork(Network network) {
        return ipAddressInNetwork.get(network) != null;
    }

    public void addIpAddressInNetwork(IPAddress ipAddress, Network network) {
        ipAddressInNetwork.put(network, ipAddress);
    }
}