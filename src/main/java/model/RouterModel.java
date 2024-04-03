package model;

import common.NetworkDeviceType;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RouterModel extends NetworkDeviceModel {
    private final RoutingTable routingTable;

    private final IPAddress currentAvailableLanNetworkIp = new IPAddress(192, 168, 1, 0);
    private final SubnetMask defaultLanSubnetMask = new SubnetMask(24);
    private final ArrayList<LanNetwork> lanNetworks = new ArrayList<>();
    private final Map<Network, RouterInterface> routerInterfaces = new HashMap<>();

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
        if (receivedEntry.getDestinationNetwork().getNetworkType() == NetworkType.LAN) {
            return;
        }
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

    public LanNetwork createLanNetwork() {
        IPAddress networkIp = new IPAddress(currentAvailableLanNetworkIp);
        currentAvailableLanNetworkIp.incrementOctet(3, 1);

        LanNetwork lanNetwork = new LanNetwork(networkIp, defaultLanSubnetMask);
        lanNetworks.add(lanNetwork);

        IPAddress interfaceIp = lanNetwork.getNextAvailableIpAddress();
        RouterInterface routerInterface = new RouterInterface(interfaceIp, new MACAddress(UUID.randomUUID().toString()));
        routerInterfaces.put(lanNetwork, routerInterface);

        return lanNetwork;
    }

    public ArrayList<LanNetwork> getLanNetworks() {
        return lanNetworks;
    }

    public IPAddress getIpAddressInNetwork(Network network) {
        if (!routerInterfaces.containsKey(network)) {
            return new IPAddress(0, 0, 0, 0);
        }
        return routerInterfaces.get(network).ipAddress;
    }

    public boolean isInNetwork(Network network) {
        return routerInterfaces.get(network) != null;
    }

    public void addRouterInterface(RouterInterface routerInterface, Network network) {
        routerInterfaces.put(network, routerInterface);
    }

    public Map<Network, RouterInterface> getRouterInterfaces() {
        return routerInterfaces;
    }

    public IPAddress getCurrentAvailableLanNetworkIp() {
        return currentAvailableLanNetworkIp;
    }

    public Pair<LanNetwork, IPAddress> getDirectConnectionLan() {
        return new Pair<>(lanNetworks.getFirst(), routerInterfaces.get(lanNetworks.getFirst()).ipAddress);
    }
}