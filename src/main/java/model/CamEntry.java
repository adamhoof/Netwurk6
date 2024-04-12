package model;

/**
 * Represents an entry in a CAM table, linking a MAC address to a port number.
 */
public class CamEntry {
    MACAddress macAddress;
    int port;

    /**
     * Constructs a CAM table entry with a MAC address and port number.
     *
     * @param macAddress the MAC address
     * @param port the port number associated with the MAC address
     */
    public CamEntry(MACAddress macAddress, int port) {
        this.macAddress = macAddress;
        this.port = port;
    }
}
