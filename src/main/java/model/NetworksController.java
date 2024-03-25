package model;

import java.util.ArrayList;

public class NetworksController {
    IPAddress defaultLanIpAddress = new IPAddress(192, 168, 1, 0);
    IPAddress currentAvailableWanNetworkAddress = new IPAddress(50, 0, 0, 0);
    SubnetMask defaultLanSubnetMask = new SubnetMask(24);
    SubnetMask defaultWanRouterLinkSubnetMask = new SubnetMask(30);

    ArrayList<Network> networks = new ArrayList<>();

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

    public Network createDefaultLanNetwork() {
        Network network = new Network(getDefaultLanNetworkIpAddress(), getDefaultLanSubnetMask());
        networks.add(network);
        return network;
    }

    public IPAddress reserveIpAddress(Network network) {
        return network.getNextAvailableIpAddress();
    }

    public IPAddress getDefaultLanNetworkIpAddress() {
        return defaultLanIpAddress;
    }

    public IPAddress getCurrentAvailableWanNetworkAddress() {
        return currentAvailableWanNetworkAddress;
    }

    public void setCurrentAvailableWanNetworkAddress(IPAddress currentAvailableWanNetworkAddress) {
        this.currentAvailableWanNetworkAddress = currentAvailableWanNetworkAddress;
    }

    public SubnetMask getDefaultLanSubnetMask() {
        return defaultLanSubnetMask;
    }

    public SubnetMask getDefaultWanRouterLinkSubnetMask() {
        return defaultWanRouterLinkSubnetMask;
    }

    public void setDefaultWanRouterLinkSubnetMask(SubnetMask defaultWanRouterLinkSubnetMask) {
        this.defaultWanRouterLinkSubnetMask = defaultWanRouterLinkSubnetMask;
    }
}
