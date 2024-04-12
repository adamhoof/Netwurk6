package model;

/**
 * Represents a DHCP Discover message used during the initial stage of the DHCP handshake.
 * This message is sent by a client when it seeks to obtain an IP configuration automatically from a DHCP server.
 */
public class DhcpDiscoverMessage implements Message {
    private MACAddress sourceMac;

    /**
     * Constructs a DhcpDiscoverMessage with the client's MAC address.
     *
     * @param sourceMac the MAC address of the device requesting an IP address
     */
    public DhcpDiscoverMessage(MACAddress sourceMac){
        this.sourceMac = sourceMac;
    }

    /**
     * Gets the source MAC address of the DHCP Discover message.
     *
     * @return the MAC address of the device that sent this message
     */
    public MACAddress getSourceMac() {
        return sourceMac;
    }
}
