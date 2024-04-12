package model;

/**
 * Represents a DHCP Offer message, which is sent by a DHCP server in response to a DHCP Discover message from a client.
 * This message contains an IP address offered to the client, along with other network configuration details such as the default gateway and subnet mask.
 */
public class DhcpOfferMessage implements Message {
    private final IPAddress offeredIpAddress;
    private final IPAddress defaultGateway;
    private final SubnetMask subnetMask;

    /**
     * Constructs a DhcpOfferMessage with specified network details.
     *
     * @param offeredIpAddress the IP address being offered to the client
     * @param defaultGateway the default gateway for the network
     * @param subnetMask the subnet mask for the network
     */
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
