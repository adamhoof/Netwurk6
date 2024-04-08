package controller;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworksController {
    private final IPAddress currentAvailableWanNetworkAddress = new IPAddress(50, 0, 0, 0);
    private final SubnetMask defaultWanRouterLinkSubnetMask = new SubnetMask(30);

    private final Map<RouterModel, ArrayList<RouterModel>> routersRipConnections = new HashMap<>();

    private final ArrayList<Network> networks = new ArrayList<>();

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

    public WanNetwork createDefaultWanNetwork() {
        WanNetwork network = new WanNetwork(reserveCurrentAvailableWanLinkNetworkAddress(), getDefaultWanRouterLinkSubnetMask());
        networks.add(network);
        return network;
    }

    public IPAddress reserveIpAddressInNetwork(Network network) {
        return network.getNextAvailableIpAddress();
    }

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

    public void addRipConnection(RouterModel first, RouterModel second) {
        getRoutersRipConnections(first).add(second);
        getRoutersRipConnections(second).add(first);
    }

    public ArrayList<RouterModel> getRoutersRipConnections(RouterModel routerModel) {
        if (!routersRipConnections.containsKey(routerModel)) {
            routersRipConnections.put(routerModel, new ArrayList<>());
        }
        return routersRipConnections.get(routerModel);
    }

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

    public boolean isSameNetwork(PCModel initiator, PCModel recipient) {
        long initiatorNetworkPortion = initiator.getIpAddress().toLong() & initiator.getSubnetMask().toLong();
        long recipientNetworkPortion = recipient.getIpAddress().toLong() & recipient.getSubnetMask().toLong();
        return initiatorNetworkPortion == recipientNetworkPortion;
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }
}
