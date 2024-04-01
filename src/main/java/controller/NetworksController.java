package controller;

import common.NetworkDeviceType;
import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworksController {
    private final IPAddress defaultLanIpAddress = new IPAddress(192, 168, 1, 0);
    private IPAddress currentAvailableWanNetworkAddress = new IPAddress(50, 0, 0, 0);
    private final SubnetMask defaultLanSubnetMask = new SubnetMask(24);
    private final SubnetMask defaultWanRouterLinkSubnetMask = new SubnetMask(30);

    private final Map<NetworkDeviceModel, ArrayList<NetworkDeviceModel>> networkConnections = new HashMap<>();

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

    public LanNetwork createDefaultLanNetwork() {
        LanNetwork network = new LanNetwork(getDefaultLanNetworkIpAddress(), getDefaultLanSubnetMask());
        networks.add(network);
        return network;
    }

    public WanNetwork createDefaultWanNetwork() {
        WanNetwork network = new WanNetwork(reserveCurrentAvailableWanLinkNetworkAddress(), getDefaultWanRouterLinkSubnetMask());
        networks.add(network);
        return network;
    }

    public IPAddress reserveIpAddress(Network network) {
        return network.getNextAvailableIpAddress();
    }

    public boolean addNetworkConnection(NetworkDeviceModel first, NetworkDeviceModel second) {
        boolean firstEmpty = !networkConnections.containsKey(first);
        boolean secondEmpty = !networkConnections.containsKey(second);

        if (!firstEmpty && first.getNetworkDeviceType() == NetworkDeviceType.PC) {
            return false;
        }
        if (!secondEmpty && second.getNetworkDeviceType() == NetworkDeviceType.PC) {
            return false;
        }

        if (firstEmpty) {
            networkConnections.put(first, new ArrayList<>());
        }
        networkConnections.get(first).add(second);

        if (secondEmpty) {
            networkConnections.put(second, new ArrayList<>());
        }
        networkConnections.get(second).add(first);

        return true;
    }

    public void createWanLink(RouterModel first, RouterModel second) {
        WanNetwork network = createDefaultWanNetwork();
        IPAddress firstRouterIpAddress = reserveIpAddress(network);
        IPAddress secondRouterIpAddress = reserveIpAddress(network);

        first.addIpAddressInNetwork(firstRouterIpAddress, network);
        second.addIpAddressInNetwork(secondRouterIpAddress, network);

        first.appendRoutingTable(new RouteEntry(network, firstRouterIpAddress, 0));
        second.appendRoutingTable(new RouteEntry(network, secondRouterIpAddress, 0));

        addRipConnection(first, second);
    }

    public void addRipConnection(RouterModel first, RouterModel second) {
        getRoutersRipConnections(first).add(second);
        getRoutersRipConnections(second).add(first);
    }

    public ArrayList<RouterModel> getRoutersRipConnections(RouterModel routerModel) {
        if (!routersRipConnections.containsKey(routerModel)){
            routersRipConnections.put(routerModel, new ArrayList<>());
        }
        return routersRipConnections.get(routerModel);
    }

    public IPAddress getDefaultLanNetworkIpAddress() {
        return defaultLanIpAddress;
    }

    public SubnetMask getDefaultLanSubnetMask() {
        return defaultLanSubnetMask;
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
}
