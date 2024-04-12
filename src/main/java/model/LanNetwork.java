package model;

/**
 * Represents a Local Area Network (LAN) within a network simulation.
 * Extends the generic Network class with specific settings suitable for LAN environments.
 */
public class LanNetwork extends Network{
    /**
     * Constructs a LanNetwork with specified IP address and subnet mask.
     *
     * @param networkIpAddress the network IP address for the LAN
     * @param subnetMask the subnet mask for the LAN
     */
    public LanNetwork(IPAddress networkIpAddress, SubnetMask subnetMask) {
        super(networkIpAddress, subnetMask, NetworkType.LAN);
    }
}
