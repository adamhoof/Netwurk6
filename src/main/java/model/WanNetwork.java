package model;

public class WanNetwork extends Network{
    public WanNetwork(IPAddress networkIpAddress, SubnetMask subnetMask) {
        super(networkIpAddress, subnetMask, NetworkType.WAN);
    }
}
