package model;

/**
 * Represents an ARP request message in the network.
 */
public class ArpRequestMessage implements Message {
    IPAddress requestedIpAddress;
    IPAddress requesterIpAddress;
    MACAddress requesterMacAddress;

    /**
     * Constructs an ARP request message with specified details.
     *
     * @param requestedIpAddress the IP address being queried in the ARP request
     * @param requesterIpAddress the IP address of the requester
     * @param requesterMacAddress the MAC address of the requester
     */
    public ArpRequestMessage(IPAddress requestedIpAddress, IPAddress requesterIpAddress, MACAddress requesterMacAddress) {
        this.requestedIpAddress = requestedIpAddress;
        this.requesterMacAddress = requesterMacAddress;
        this.requesterIpAddress = requesterIpAddress;
    }

    public IPAddress getRequestedIpAddress() {
        return requestedIpAddress;
    }

    public MACAddress getRequesterMacAddress() {
        return requesterMacAddress;
    }

    public IPAddress getRequesterIpAddress() {
        return requesterIpAddress;
    }
}
