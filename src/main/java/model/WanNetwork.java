package model;

/**
 * Represents a WAN network in the simulation, characterized by its unique network IP address and subnet mask.
 */
public class WanNetwork extends Network {
    /**
     * Constructs a WanNetwork with a specified network IP address and subnet mask.
     *
     * @param networkIpAddress The IP address of the WAN network.
     * @param subnetMask The subnet mask of the WAN network.
     */
    public WanNetwork(IPAddress networkIpAddress, SubnetMask subnetMask) {
        super(networkIpAddress, subnetMask, NetworkType.WAN);
    }
}
