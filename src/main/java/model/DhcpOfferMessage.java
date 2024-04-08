package model;

public class DhcpOfferMessage implements Message {
    private IPAddress offeredIpAddress;
    private IPAddress defaultGateway;

    private SubnetMask subnetMask;

    public DhcpOfferMessage(IPAddress offeredIpAddress, IPAddress defaultGateway, SubnetMask subnetMask) {
        this.offeredIpAddress = offeredIpAddress;
        this.defaultGateway = defaultGateway;
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
