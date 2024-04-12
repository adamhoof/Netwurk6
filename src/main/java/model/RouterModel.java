package model;

import common.AutoNameGenerator;
import common.NetworkDeviceType;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a router in a network simulation, managing LAN networks, routing tables,
 * and interfaces to other network devices.
 */
public class RouterModel extends NetworkDeviceModel {
    private final RoutingTable routingTable;
    private final IPAddress currentAvailableLanNetworkIp = new IPAddress(192, 168, 1, 0);
    private final SubnetMask defaultLanSubnetMask = new SubnetMask(24);
    private final ArrayList<LanNetwork> lanNetworks = new ArrayList<>();
    private final ConcurrentHashMap<Network, RouterInterface> routerInterfaces = new ConcurrentHashMap<>();
    private final ArpCache arpCache;
    private final HashSet<NetworkDeviceModel> directConnections = new HashSet<>();

    /**
     * Constructor for RouterModel, initializes a router with a UUID and MAC address.
     *
     * @param uuid The unique identifier for the router.
     * @param macAddress The MAC address of the router.
     */
    public RouterModel(UUID uuid, MACAddress macAddress) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER);
        this.routingTable = new RoutingTable();
        this.arpCache = new ArpCache();
    }

    /**
     * Constructor for RouterModel, initializes a router with a UUID, MAC address, and a name.
     *
     * @param uuid The unique identifier for the router.
     * @param macAddress The MAC address of the router.
     * @param name The name of the router.
     */
    public RouterModel(UUID uuid, MACAddress macAddress, String name) {
        super(uuid, macAddress, NetworkDeviceType.ROUTER, name);
        this.routingTable = new RoutingTable();
        this.arpCache = new ArpCache();
    }

    /**
     * Adds a route entry to the router's routing table.
     *
     * @param routeEntry The route entry to be added.
     */
    public void appendRoutingTable(RouteEntry routeEntry) {
        routingTable.addEntry(routeEntry);
    }

    /**
     * Returns the routing table of the router.
     *
     * @return The current routing table.
     */
    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    /**
     * Processes a received routing entry and updates the routing table accordingly.
     *
     * @param receivedEntry The received routing entry.
     * @param sourceIPAddress The IP address from which the routing information was received.
     */
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

    /**
     * Updates the router's routing table based on a received routing table from another router.
     *
     * @param receivedRoutingTable The routing table received from another router.
     * @param sourceIPAddress The source IP address of the router that sent the routing table.
     */
    public void updateRoutingTable(RoutingTable receivedRoutingTable, IPAddress sourceIPAddress) {
        for (RouteEntry entry : receivedRoutingTable.getEntries()) {
            processReceivedEntry(entry, sourceIPAddress);
        }
    }

    /**
     * Creates a new LAN network, generates a new router interface for it, and adds it to the router.
     *
     * @return The newly created LAN network.
     */
    public LanNetwork createLanNetwork() {
        IPAddress networkIp = new IPAddress(currentAvailableLanNetworkIp);
        currentAvailableLanNetworkIp.incrementOctet(3, 1);

        LanNetwork lanNetwork = new LanNetwork(networkIp, defaultLanSubnetMask);
        lanNetworks.add(lanNetwork);

        IPAddress interfaceIp = lanNetwork.getNextAvailableIpAddress();
        RouterInterface routerInterface = new RouterInterface(UUID.randomUUID(), interfaceIp, new MACAddress(UUID.randomUUID().toString()), this, lanNetwork);
        routerInterface.setName(AutoNameGenerator.generateRouterInterfaceName());
        routerInterfaces.put(lanNetwork, routerInterface);

        return lanNetwork;
    }

    /**
     * Retrieves the router interface associated with a specific network.
     *
     * @param network The network for which the router interface is needed.
     * @return The router interface connected to the specified network.
     */
    public RouterInterface getNetworksRouterInterface(Network network) {
        for (Network networkFromSet : routerInterfaces.keySet()) {
            if (networkFromSet.getNetworkIpAddress() == network.getNetworkIpAddress()) {
                return routerInterfaces.get(networkFromSet);
            }
        }
        return null;
    }

    /**
     * Returns all LAN networks managed by this router.
     *
     * @return A list of LAN networks.
     */
    public ArrayList<LanNetwork> getLanNetworks() {
        return lanNetworks;
    }

    /**
     * Retrieves the IP address of this router within a specified network.
     *
     * @param network The network in which the IP address is required.
     * @return The IP address of this router within the specified network.
     */
    public IPAddress getIpAddressInNetwork(Network network) {
        if (!routerInterfaces.containsKey(network)) {
            return new IPAddress(0, 0, 0, 0);
        }
        return routerInterfaces.get(network).getIpAddress();
    }

    /**
     * Checks if the router is part of the specified network.
     *
     * @param network The network to check.
     * @return true if the router is part of the specified network, otherwise false.
     */
    public boolean isInNetwork(Network network) {
        return routerInterfaces.get(network) != null;
    }

    /**
     * Adds a router interface to a specified network.
     *
     * @param routerInterface The router interface to add.
     * @param network The network to which the router interface should be added.
     */
    public void addRouterInterface(RouterInterface routerInterface, Network network) {
        routerInterfaces.put(network, routerInterface);
    }

    /**
     * Retrieves all router interfaces managed by this router.
     *
     * @return A concurrent hash map of networks to their corresponding router interfaces.
     */
    public ConcurrentHashMap<Network, RouterInterface> getRouterInterfaces() {
        return routerInterfaces;
    }

    /**
     * Retrieves the current IP address available for LAN networks.
     *
     * @return The current available LAN network IP.
     */
    public IPAddress getCurrentAvailableLanNetworkIp() {
        return currentAvailableLanNetworkIp;
    }

    /**
     * Retrieves the direct connection LAN network IP and interface.
     *
     * @return A pair containing the LAN network and its corresponding IP address.
     */
    public Pair<LanNetwork, IPAddress> getDirectConnectionLanNetworkIp() {
        return new Pair<>(lanNetworks.getFirst(), routerInterfaces.get(lanNetworks.getFirst()).getIpAddress());
    }

    /**
     * Retrieves the router interface for the first direct connection LAN network.
     *
     * @return The router interface for the direct connection LAN network.
     */
    public RouterInterface getDirectConnectionLanInterface() {
        return routerInterfaces.get(lanNetworks.getFirst());
    }

    /**
     * Adds a network device model to the direct connections of this router.
     *
     * @param networkDeviceModel The network device model to connect.
     * @return true if the connection was successful.
     */
    @Override
    public boolean addConnection(NetworkDeviceModel networkDeviceModel) {
        directConnections.add(networkDeviceModel);
        if (networkDeviceModel instanceof PCModel pcModel) {
            RouterInterface routerInterface = getDirectConnectionLanInterface();
            routerInterface.addConnection(pcModel);
            return pcModel.addConnection(routerInterface);
        } else if (networkDeviceModel instanceof SwitchModel switchModel) {
            LanNetwork lanNetwork = createLanNetwork();
            RouterInterface routerInterface = getNetworksRouterInterface(lanNetwork);
            routerInterface.addConnection(switchModel);
            return switchModel.addConnection(routerInterface);
        }
        return true;
    }

    /**
     * Updates the ARP cache with a new IP and MAC address mapping.
     *
     * @param ipAddress The IP address to map.
     * @param macAddress The MAC address to map to the IP address.
     */
    public void updateArp(IPAddress ipAddress, MACAddress macAddress) {
        arpCache.addEntry(ipAddress, macAddress);
    }

    /**
     * Queries the ARP cache for a MAC address associated with an IP address.
     *
     * @param ipAddress The IP address for which to find the MAC address.
     * @return The MAC address associated with the specified IP address.
     */
    public MACAddress queryArp(IPAddress ipAddress) {
        return arpCache.getMAC(ipAddress);
    }
}
