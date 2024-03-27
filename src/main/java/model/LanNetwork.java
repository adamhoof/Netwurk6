package model;

public class LanNetwork extends Network{
    public LanNetwork(IPAddress networkIpAddress, SubnetMask subnetMask) {
        super(networkIpAddress, subnetMask, NetworkType.LAN);
    }
}
