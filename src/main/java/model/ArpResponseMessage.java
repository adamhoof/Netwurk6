package model;

/**
 * Represents an ARP response message in the network.
 */
public class ArpResponseMessage implements Message {
    MACAddress requestedMacAddress;

    /**
     * Constructs an ARP response message with the specified MAC address.
     *
     * @param requestedMacAddress the MAC address to be sent in the response
     */
    public ArpResponseMessage(MACAddress requestedMacAddress) {
        this.requestedMacAddress = requestedMacAddress;
    }

    public MACAddress getRequestedMacAddress() {
        return requestedMacAddress;
    }
}
