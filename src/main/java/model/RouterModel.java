package model;

import common.AutoNameGenerator;
import common.NetworkDeviceType;
import javafx.util.Pair;

import java.util.*;

public class RouterModel extends NetworkDeviceModel {
    private final RoutingTable routingTable;

    private final IPAddress currentAvailableLanNetworkIp = new IPAddress(192, 168, 1, 0);
    private final SubnetMask defaultLanSubnetMask = new SubnetMask(24);
    private final ArrayList<LanNetwork> lanNetworks = new ArrayList<>();
    private final Map<Network, RouterInterface> routerInterfaces = new HashMap<>();

    private final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();

    public RouterModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER);
        this.routingTable = new RoutingTable();
    }

    public RouterModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER, name);
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
        RouterInterface routerInterface = new RouterInterface(UUID.randomUUID(), interfaceIp, new MACAddress(UUID.randomUUID().toString()), this);
        routerInterface.setName(AutoNameGenerator.generateRouterInterfaceName());
        routerInterfaces.put(lanNetwork, routerInterface);

        return lanNetwork;
    }

    public RouterInterface getNetworksRouterInterface(Network network) {
        for (Network networkFromSet : routerInterfaces.keySet()) {
            if (networkFromSet.getNetworkIpAddress() == network.getNetworkIpAddress()){
                return routerInterfaces.get(networkFromSet);
            }
        }
        return null;
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

    public Pair<LanNetwork, IPAddress> getDirectConnectionLanNetworkIp() {
        return new Pair<>(lanNetworks.getFirst(), routerInterfaces.get(lanNetworks.getFirst()).ipAddress);
    }

    public RouterInterface getDirectConnectionLanInterface(){
        return routerInterfaces.get(lanNetworks.getFirst());
    }

    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        directConnections.add(networkDeviceModel);
        if (networkDeviceModel instanceof PCModel pcModel) {
            RouterInterface routerInterface = getDirectConnectionLanInterface();
            routerInterface.addConnection(pcModel);
            return networkDeviceModel.addConnection(routerInterface);
        } else if (networkDeviceModel instanceof SwitchModel switchModel) {
            LanNetwork lanNetwork = createLanNetwork();
            RouterInterface routerInterface = getNetworksRouterInterface(lanNetwork);
            routerInterface.addConnection(switchModel);
            return networkDeviceModel.addConnection(routerInterface);
        }
        return true;
    }
}