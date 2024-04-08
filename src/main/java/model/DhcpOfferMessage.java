package model;

public class DhcpOfferMessage implements Message {
    private final IPAddress offeredIpAddress;
    private final IPAddress defaultGateway;

    private final SubnetMask subnetMask;

    public DhcpOfferMessage(IPAddress offeredIpAddress, IPAddress defaultGateway, SubnetMask subnetMask) {
        this.offeredIpAddress = offeredIpAddress;
        this.defaultGateway = defaultGateway;
        this.subnetMask = subnetMask;
    }

    public IPAddress getOfferedIpAddress() {
        return offeredIpAddress;
    }

    public IPAddress getDefaultGateway() {
        return defaultGateway;
    }

    public SubnetMask getSubnetMask() {
        return subnetMask;
    }
}
