package controller;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the networking aspects of the simulation, including IP address allocation and network connections.
 */
public class NetworksController {
    private final IPAddress currentAvailableWanNetworkAddress = new IPAddress(50, 0, 0, 0);
    private final SubnetMask defaultWanRouterLinkSubnetMask = new SubnetMask(30);

    private final Map<RouterModel, ArrayList<RouterModel>> routersRipConnections = new HashMap<>();

    private final ArrayList<Network> networks = new ArrayList<>();

    /**
     * Reserves and updates the next available WAN network IP address based on the subnet mask size.
     *
     * @return The newly reserved IP address.
     */
    public IPAddress reserveCurrentAvailableWanLinkNetworkAddress() {
        //TODO check if we are out of increments in relevant octet
        int octetToIncrement = 0;
        IPAddress available = new IPAddress(currentAvailableWanNetworkAddress);

        long subnetMaskSize = defaultWanRouterLinkSubnetMask.getSize();
        if (subnetMaskSize < 8) {
            octetToIncrement = 1;
        } else if (subnetMaskSize > 8 && subnetMaskSize <= 16) {
            octetToIncrement = 2;
        } else if (subnetMaskSize > 16 && subnetMaskSize <= 24) {
            octetToIncrement = 3;
        } else if (subnetMaskSize > 24 && subnetMaskSize <= 32) {
            octetToIncrement = 4;
        }
        int incrementBy = (int) Math.pow(((32 - defaultWanRouterLinkSubnetMask.getSize()) % 4), 2);
        if (incrementBy == 0) {
            incrementBy += 1;
        }
        currentAvailableWanNetworkAddress.incrementOctet(octetToIncrement, incrementBy);
        return available;
    }

    /**
     * Creates a default WAN network using the current available WAN IP.
     *
     * @return The created WAN network.
     */
    public WanNetwork createDefaultWanNetwork() {
        WanNetwork network = new WanNetwork(reserveCurrentAvailableWanLinkNetworkAddress(), getDefaultWanRouterLinkSubnetMask());
        networks.add(network);
        return network;
    }

    /**
     * Reserves the next available IP address within a specified network.
     *
     * @param network The network from which to reserve an IP address.
     * @return The reserved IP address.
     */
    public IPAddress reserveIpAddressInNetwork(Network network) {
        return network.getNextAvailableIpAddress();
    }

    /**
     * Establishes a WAN link between two routers, configuring their interfaces and routing tables.
     *
     * @param first The first router in the WAN link.
     * @param second The second router in the WAN link.
     */
    public void createWanLink(RouterModel first, RouterModel second) {
        WanNetwork network = createDefaultWanNetwork();
        IPAddress firstRouterIpAddress = reserveIpAddressInNetwork(network);
        IPAddress secondRouterIpAddress = reserveIpAddressInNetwork(network);

        first.addRouterInterface(new RouterInterface(UUID.randomUUID(), firstRouterIpAddress, new MACAddress(UUID.randomUUID().toString()), first, network), network);
        second.addRouterInterface(new RouterInterface(UUID.randomUUID(), secondRouterIpAddress, new MACAddress(UUID.randomUUID().toString()), second, network), network);

        first.appendRoutingTable(new RouteEntry(network, firstRouterIpAddress, 0));
        second.appendRoutingTable(new RouteEntry(network, secondRouterIpAddress, 0));

        addRipConnection(first, second);
    }

    /**
     * Adds a RIP routing connection between two routers.
     *
     * @param first The first router.
     * @param second The second router.
     */
    public void addRipConnection(RouterModel first, RouterModel second) {
        getRoutersRipConnections(first).add(second);
        getRoutersRipConnections(second).add(first);
    }

    /**
     * Retrieves the RIP connections associated with a specific router.
     *
     * @param routerModel The router model.
     * @return A list of routers connected via RIP to the specified router.
     */
    public ArrayList<RouterModel> getRoutersRipConnections(RouterModel routerModel) {
        if (!routersRipConnections.containsKey(routerModel)) {
            routersRipConnections.put(routerModel, new ArrayList<>());
        }
        return routersRipConnections.get(routerModel);
    }

    /**
     * Retrieves the subnet mask used for WAN router links.
     *
     * @return The subnet mask.
     */
    public SubnetMask getDefaultWanRouterLinkSubnetMask() {
        return defaultWanRouterLinkSubnetMask;
    }

    public Network getSharedNetwork(RouterModel first, RouterModel second) {
        for (Network network : networks) {
            if (first.isInNetwork(network) && second.isInNetwork(network)) {
                return network;
            }
        }
        return null;
    }

    /**
     * Determines if two network devices are on the same network.
     *
     * @param initiator The initiating network device.
     * @param recipient The recipient network device.
     * @return true if both devices are on the same network, false otherwise.
     */
    public boolean isSameNetwork(PCModel initiator, PCModel recipient) {
        long initiatorNetworkPortion = initiator.getIpAddress().toLong() & initiator.getSubnetMask().toLong();
        long recipientNetworkPortion = recipient.getIpAddress().toLong() & recipient.getSubnetMask().toLong();
        return initiatorNetworkPortion == recipientNetworkPortion;
    }

    /**
     * Retrieves a list of all networks managed by this controller.
     *
     * @return A list of networks.
     */
    public ArrayList<Network> getNetworks() {
        return networks;
    }
}
